package org.towerhawk.monitor.check.run.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

class ConcurrentCheckRunAccumulator implements CheckRunAccumulator {

	private Logger log = LoggerFactory.getLogger(ConcurrentCheckRunAccumulator.class);
	private Collection<CheckRun> checkRuns = new ConcurrentSkipListSet<CheckRun>();
	private Set<Check> checkSet = new LinkedHashSet<>();
	private CountDownLatch latch;
	private Collection<ConcurrentCheckRunHandler> handlers = new ConcurrentLinkedQueue<>();

	public void accumulate(CheckRun checkRun) {
		log.debug("Accumulating CheckRun for {}", checkRun.getCheck().getId());
		checkRuns.add(checkRun);
		latch.countDown();
	}

	public ConcurrentCheckRunAccumulator(Collection<Check> checks) {
		checkSet.addAll(checks);
		latch = new CountDownLatch(checkSet.size());
	}

	@Override
	public List<CheckRun> waitForChecks() throws InterruptedException {
		log.debug("Waiting for checks");
		latch.await();
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

	public void cancelChecks() {
		handlers.forEach(h -> {
			try {
				Future<CheckRun> future = h.getCheckRunFuture();
				future.cancel(true);
			} catch (InterruptedException e) {
				log.warn("Got interrupted trying to cancel futures");
			}
		});
	}

}
