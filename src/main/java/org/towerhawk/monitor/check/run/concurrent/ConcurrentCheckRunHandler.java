package org.towerhawk.monitor.check.run.concurrent;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRun;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

@Slf4j
class ConcurrentCheckRunHandler implements Callable<CheckRun>, Comparable<ConcurrentCheckRunHandler> {

	@Getter
	private Check check;
	private ConcurrentCheckRunAccumulator accumulator;
	private ConcurrentCheckInterruptor interruptor;
	private Future<CheckRun> checkRunFuture = null;
	private CountDownLatch latch = new CountDownLatch(1);
	private long timeoutEpoch;

	ConcurrentCheckRunHandler(@NonNull Check check, @NonNull ConcurrentCheckRunAccumulator accumulator, @NonNull ConcurrentCheckInterruptor interruptor) {
		this.check = check;
		this.accumulator = accumulator;
		this.interruptor = interruptor;
	}

	void setCheckRunFuture(Future<CheckRun> checkRunFuture) {
		if (checkRunFuture != null) {
			log.debug("Setting future for {}", check.getId());
			this.checkRunFuture = checkRunFuture;
			latch.countDown();
		}
	}

	Future<CheckRun> getCheckRunFuture() throws InterruptedException {
		if (checkRunFuture == null) {
			log.debug("Waiting on future for {}", check.getId());
			latch.await();
		}
		return this.checkRunFuture;
	}

	long getTimeUntilTimeout() {
		return timeoutEpoch - System.currentTimeMillis();
	}

	void cancel() {
		log.warn("Cancelling check {}", check.getId());
		try {
			getCheckRunFuture().cancel(true);
		} catch (InterruptedException e) {
			log.warn("Got interrupted waiting for future of check {} to return", check.getId());
		}
	}

	@Override
	public CheckRun call() throws Exception {
		timeoutEpoch = System.currentTimeMillis() + check.getTimeoutMs();
		CheckRun checkRun = null;
		try {
			log.debug("Submitting handler for check {} to interruptor", check.getId());
			interruptor.submit(this);
			log.debug("Running check {}", check.getId());
			checkRun = check.run();
		} catch (Exception e) {
			log.error("Check {} completed exceptionally", check.getId(), e);
		} finally {
			log.debug("Accumulating CheckRun for {}", check.getId());
			//TODO figure out how to make this less hackish
			if (checkRun == null) {
				//Call getLastCheckRun since that should always be set inside of run()
				checkRun = check.getLastCheckRun();
			}
			accumulator.accumulate(checkRun);
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
