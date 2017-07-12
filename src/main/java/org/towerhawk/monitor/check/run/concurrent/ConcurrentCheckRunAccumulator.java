package org.towerhawk.monitor.check.run.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunAccumulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
class ConcurrentCheckRunAccumulator implements CheckRunAccumulator {

	private Collection<CheckRun> checkRuns = new ConcurrentSkipListSet<>();
	private Set<Check> checkSet = new LinkedHashSet<>();
	private CountDownLatch latch;
	private Collection<ConcurrentCheckRunHandler> handlers = new ConcurrentLinkedQueue<>();

	public void accumulate(CheckRun checkRun) {
		log.debug("Accumulating CheckRun for {}", checkRun.getCheck().getId());
		// Can throw a null-pointer exception that is difficult to track down
		// since the main thread can end up waiting on this forever.
		try {
			checkRuns.add(checkRun);
		} catch (Exception e) {
			log.error("Unable to accumulate check {}", checkRun.getCheck().getId());
		} finally {
			latch.countDown();
		}
	}

	public ConcurrentCheckRunAccumulator(Collection<Check> checks) {
		checkSet.addAll(checks);
		latch = new CountDownLatch(checkSet.size());
	}

	@Override
	public List<CheckRun> waitForChecks() throws InterruptedException {
		log.debug("Waiting for checks");
		try {
			latch.await();
		} catch (InterruptedException e) {
			log.warn("Got interrupted waiting for checks {} to complete", checkSet.stream().map(Check::getId).collect(Collectors.toList()));
			cancelChecks();
		}
		return getChecks();
	}

	@Override
	public List<CheckRun> getChecks() {
		List<CheckRun> checkRunList = new ArrayList<>(checkSet.size());
		checkRunList.addAll(checkRuns);
		Collections.sort(checkRunList);
		return checkRunList;
	}

	public void addHandler(ConcurrentCheckRunHandler handler) {
		handlers.add(handler);
	}

	@Override
	public void cancelChecks() {
		handlers.forEach(ConcurrentCheckRunHandler::cancel);
		//Give each of the handlers a grace period to clean up and accumulate
		try {
			latch.await(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.warn("Got interrupted waiting for futures to finish during grace period");
		}
	}

}
