package org.towerhawk.monitor.check.run.concurrent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.logging.CheckMDC;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@Extension
@TowerhawkType({"asynchronous","async"})
public class ConcurrentCheckRunner implements AsynchronousCheckRunner {

	@Getter
	private final ExecutorService checkRunService;
	@Getter
	private final ConcurrentCheckInterruptor interruptor;

	public ConcurrentCheckRunner(ConcurrentCheckInterruptor interruptor, ExecutorService checkRunService) {
		this.interruptor = interruptor;
		this.checkRunService = checkRunService;
	}

	@Override
	public ConcurrentCheckRunAccumulator runChecksAsync(Collection<Check> checks, RunContext runContext) {
		List<Check> checkList = new ArrayList<>(checks);
		Collections.sort(checkList);
		ConcurrentCheckRunAccumulator accumulator = new ConcurrentCheckRunAccumulator(checkList);
		log.debug("Building handlers");
		Collection<ConcurrentCheckRunHandler> handlers = checkList.stream().map(c -> new ConcurrentCheckRunHandler(c, accumulator, interruptor, runContext)).collect(Collectors.toList());
		handlers.forEach(h -> {
			try {
				CheckMDC.put(h.getCheck());
				if (runContext.shouldRun() && h.getCheck().canRun()) {
					log.debug("Submitting handler for {}", h.getCheck().getFullName());
					Future<CheckRun> handlerFuture = checkRunService.submit(h);
					h.setCheckRunFuture(handlerFuture);
					accumulator.addHandler(h);
				} else { //Shortcut calling the shouldRun method just to get a cached result
					accumulator.accumulate(h.getCheck().getLastCheckRun());
				}
			} finally {
				CheckMDC.remove();
			}
		});
		log.debug("Returning accumulator");
		return accumulator;
	}

	@Override
	public List<CheckRun> runChecks(Collection<Check> checks, RunContext runContext) {
		ConcurrentCheckRunAccumulator accumulator = runChecksAsync(checks, runContext);
		try {
			return accumulator.waitForChecks();
		} catch (InterruptedException e) {
			accumulator.cancelChecks();
			try {
				return accumulator.waitForChecks();
			} catch (InterruptedException e1) {
				log.error("Got interrupted after cancelling checks and waiting for return", e);
				return accumulator.getChecks();
			}
		}
	}
}

