package org.towerhawk.check.run;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.check.Check;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Named
public class CheckRunner {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final ExecutorService checkRunService;
	private final ExecutorService checkRunInterruptor;

	@Inject
	public CheckRunner(ExecutorService checkRunService, ExecutorService checkRunInterruptor) {
		this.checkRunService = checkRunService;
		this.checkRunInterruptor = checkRunInterruptor;
	}

	public CompletionService<CheckRun> runChecksAsync(Collection<Check> checks) {
		CompletionService<CheckRun> completionService = new ExecutorCompletionService<CheckRun>(checkRunInterruptor);
		List<Check> checkList = new ArrayList(checks);
		Collections.sort(checkList);
		Map<Check, Future<CheckRun>> futureCheckRuns = new LinkedHashMap<>();
		log.trace("Submitting checks to completion service");
		checkList.forEach(check -> futureCheckRuns.put(check, completionService.submit(new CheckRunInterruptor(check)::run)));
		return completionService;
	}

	public List<CheckRun> runChecks(Collection<Check> checks) {
		CompletionService<CheckRun> completionService = runChecksAsync(checks);
		List<CheckRun> checkRuns = new ArrayList<>(checks.size());
		for (int i = 0; i < checks.size(); i++) {
			try {
				log.trace("Getting results");
				CheckRun checkRun = completionService.take().get();
				if (checkRun != null) {
					log.trace("Got result for {}", checkRun.check().getId());
					checkRuns.add(checkRun);
				} else {
					log.error("Unhandled error was thrown");
				}
			} catch (InterruptedException e) {
				log.warn("Thread interrupted while waiting for checks to complete. Returning immediately.");
				break;
			} catch (ExecutionException e) {
				log.error("Check::run returned unhandled exception");
			}
		}
		log.trace("Returning results");
		//Return checksRuns in sorted order
		Collections.sort(checkRuns);
		return checkRuns;
	}

	private class CheckRunInterruptor {

		private Check check;

		CheckRunInterruptor(Check check) {
			this.check = check;
		}

		Check getCheck() {
			return check;
		}

		CheckRun run() {
			log.trace("Submitting actual job for {}", check.getId());
			Future<CheckRun> checkRunFuture = checkRunService.submit(check::run);
			long graceMs = 500;
			long executionCleanupMs = 3000;
			CheckRun checkRun = null;
			try {
				log.trace("Getting actual job for {}", check.getId());
				checkRun = checkRunFuture.get(check.getTimeoutMs() + graceMs, TimeUnit.MILLISECONDS);
				log.trace("Got actual job for {}", check.getId());
			} catch (InterruptedException e) {
				log.warn("Thread interrupted while waiting for Check::run to execute. Assuming shutdown in progress so not waiting for execution to return.");
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				log.error("Check::run returned unhandled exception", e);
			} catch (TimeoutException e) {
				log.warn("Cancelling future for {}", check.getId());
				checkRunFuture.cancel(true);
				while (!checkRunFuture.isDone()) {
					try {
						checkRun = checkRunFuture.get(executionCleanupMs, TimeUnit.MILLISECONDS);
						log.debug("Got checkRun for {} after calling cancel", check.getId());
						break;
					} catch (InterruptedException e1) {
						log.warn("Thread interrupted while waiting for Check::run to clean up. Assuming shutdown in progress so not waiting for execution to return.");
						Thread.currentThread().interrupt();
						break;
					} catch (ExecutionException e1) {
						log.error("Check::run returned unhandled exception during cleanup", e1);
						break;
					} catch (TimeoutException e1) {
						log.warn("Check::run has not completed after timing out and cleanup time. Waiting until it finishes before relinquishing thread");
					}
				}
			}
			return checkRun;
		}
	}
}
