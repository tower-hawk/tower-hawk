package org.towerhawk.monitor.check.run.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRun;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

class ConcurrentCheckRunHandler implements Callable<CheckRun>, Comparable<ConcurrentCheckRunHandler> {

	private Logger log = LoggerFactory.getLogger(ConcurrentCheckRunHandler.class);
	private Check check;
	private ConcurrentCheckRunAccumulator accumulator;
	private ConcurrentCheckInterruptor interruptor;
	private Future<CheckRun> checkRunFuture = null;
	private CountDownLatch latch = new CountDownLatch(1);
	private long timeoutEpoch;
	private CheckRun checkRun = null;

	public ConcurrentCheckRunHandler(Check check, ConcurrentCheckRunAccumulator accumulator, ConcurrentCheckInterruptor interruptor) {
		this.check = check;
		this.accumulator = accumulator;
		this.interruptor = interruptor;
	}

	public void setCheckRunFuture(Future<CheckRun> checkRunFuture) {
		log.debug("Setting future for {}", check.getId());
		boolean countdown = checkRunFuture == null;
		this.checkRunFuture = checkRunFuture;
		if (countdown) {
			latch.countDown();
		}
	}

	public Future<CheckRun> getCheckRunFuture() throws InterruptedException {
		if (checkRunFuture == null) {
			log.debug("Waiting on future for {}", check.getId());
			latch.await();
		}
		return this.checkRunFuture;
	}

	public long getTimeUntilTimeout() {
		return timeoutEpoch - System.currentTimeMillis();
	}

	public Check getCheck() {
		return check;
	}

	@Override
	public CheckRun call() throws Exception {
		timeoutEpoch = System.currentTimeMillis() + check.getTimeoutMs();
		try {
			log.debug("Submitting handler for check {} to interruptor", check.getId());
			interruptor.submit(this);
			log.debug("Running check {}", check.getId());
			checkRun = check.run();
		} finally {
			log.debug("Accumulating CheckRun for {}", check.getId());
			accumulator.accumulate(checkRun == null ? check.getLastCheckRun() : checkRun);
			log.debug("Removing handler for check {} from interruptor", check.getId());
			interruptor.remove(this);
		}
		return checkRun;
	}

	@Override
	public int compareTo(ConcurrentCheckRunHandler o) {
		return Long.compare(this.timeoutEpoch, o.timeoutEpoch);
	}
}
