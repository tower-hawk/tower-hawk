package org.towerhawk.monitor.check.run.concurrent;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.monitor.check.run.CheckRun;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

@Slf4j
public class ConcurrentCheckRunHandler implements Callable<CheckRun>, Comparable<ConcurrentCheckRunHandler> {

	@Getter
	private Check check;
	private CheckRun checkRun = null;
	private ConcurrentCheckRunAccumulator accumulator;
	private ConcurrentCheckInterruptor interruptor;
	private Future<CheckRun> checkRunFuture = null;
	private CountDownLatch latch = new CountDownLatch(1);
	private long timeoutEpoch;
	private RunContext runContext;

	public ConcurrentCheckRunHandler(
		@NonNull Check check,
		@NonNull ConcurrentCheckRunAccumulator accumulator,
		@NonNull ConcurrentCheckInterruptor interruptor,
		@NonNull RunContext runContext
	) {
		this.check = check;
		this.accumulator = accumulator;
		this.interruptor = interruptor;
		this.runContext = runContext;
	}

	public void setCheckRunFuture(Future<CheckRun> checkRunFuture) {
		if (checkRunFuture != null) {
			log.debug("Setting future for {}", check.getFullName());
			this.checkRunFuture = checkRunFuture;
			latch.countDown();
		}
	}

	public Future<CheckRun> getCheckRunFuture() throws InterruptedException {
		if (checkRunFuture == null) {
			log.debug("Waiting on future for {}", check.getFullName());
			latch.await();
		}
		return this.checkRunFuture;
	}

	public long getTimeUntilTimeout() {
		return timeoutEpoch - System.currentTimeMillis();
	}

	public void cancel() {
		log.warn("Cancelling check {}", check.getFullName());
		try {
			getCheckRunFuture().cancel(true);
		} catch (InterruptedException e) {
			log.warn("Got interrupted waiting for future of check {} to return", check.getFullName());
		}
	}

	@Override
	public CheckRun call() throws Exception {
		timeoutEpoch = System.currentTimeMillis() + check.getTimeoutMs();
		checkRun = null;
		try {
			log.debug("Submitting handler for check {} to interruptor", check.getFullName());
			interruptor.submit(this);
			log.debug("Running check {}", check.getFullName());
			checkRun = check.run(runContext);
		} catch (Exception e) {
			log.error("Check {} completed exceptionally", check.getFullName(), e);
		} finally {
			log.debug("Accumulating CheckRun for {}", check.getFullName());
			//TODO figure out how to make this less hackish
			if (checkRun == null) {
				//Call getLastCheckRun since that should always be set inside of run()
				checkRun = check.getLastCheckRun();
			}
			accumulator.accumulate(checkRun);
			log.debug("Removing handler for check {} from interruptor", check.getFullName());
			interruptor.remove(this);
		}
		return checkRun;
	}

	@Override
	public int compareTo(ConcurrentCheckRunHandler o) {
		return Long.compare(this.timeoutEpoch, o.timeoutEpoch);
	}
}
