package org.towerhawk.monitor.check.run.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.logging.CheckMDC;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunAccumulator;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ConcurrentCheckRunAccumulator implements CheckRunAccumulator {

	private Collection<CheckRun> checkRuns = new ConcurrentSkipListSet<>();
	private Set<Check> checkSet = new LinkedHashSet<>();
	private CountDownLatch latch;
	private Collection<ConcurrentCheckRunHandler> handlers = new ConcurrentLinkedQueue<>();

	public void accumulate(CheckRun checkRun) {
		CheckMDC.put(checkRun.getCheck());
		log.debug("Accumulating CheckRun");
		// Can throw a null-pointer exception that is difficult to track down
		// since the main thread can end up waiting on this forever.
		try {
			checkRuns.add(checkRun);
			checkSet.remove(checkRun.getCheck());
		} catch (Exception e) {
			log.error("Unable to accumulate check", e);
		} finally {
			latch.countDown();
			CheckMDC.remove();
		}
	}

	public void ignore(Check check) {
		try {
			checkSet.remove(check);
		} catch (Exception e) {
			log.error("Unable to remove check from accumulator", e);
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
			log.warn("Got interrupted waiting for checks {} to complete", checkSet.stream().map(Check::getFullName).collect(Collectors.toList()));
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
