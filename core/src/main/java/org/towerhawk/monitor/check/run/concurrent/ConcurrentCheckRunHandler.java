package org.towerhawk.monitor.check.run.concurrent;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.descriptors.Interruptable;
import org.towerhawk.monitor.check.logging.CheckMDC;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;

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
		CheckMDC.put(check);
		if (checkRunFuture != null) {
			log.debug("Setting future for check");
			this.checkRunFuture = checkRunFuture;
			latch.countDown();
		}
		CheckMDC.remove();
	}

	public Future<CheckRun> getCheckRunFuture() throws InterruptedException {
		CheckMDC.put(check);
		if (checkRunFuture == null) {
			log.debug("Waiting on future for check");
			latch.await();
		}
		CheckMDC.remove();
		return this.checkRunFuture;
	}

	public long getTimeUntilTimeout() {
		return timeoutEpoch - System.currentTimeMillis();
	}

	public void cancel() {
		CheckMDC.put(check);
		log.warn("Cancelling check");
		try {
			getCheckRunFuture().cancel(true);
		} catch (InterruptedException e) {
			log.warn("Got interrupted waiting for future of check to return");
		}
		CheckMDC.remove();
	}

	@Override
	public CheckRun call() throws Exception {
		CheckMDC.put(check);
		timeoutEpoch = System.currentTimeMillis() + (check instanceof Interruptable ? ((Interruptable)check).getTimeoutMs() : 10000);
		checkRun = null;
		try {
			log.debug("Submitting handler for check to interruptor");
			interruptor.submit(this);
			log.debug("Running check");
			checkRun = check.run(runContext);
		} catch (Exception e) {
			log.warn("Check completed exceptionally", e);
		} finally {
			log.debug("Accumulating CheckRun for check");
			//TODO figure out how to make this less hackish
			if (checkRun == null) {
				//Call getLastCheckRun since that should always be set inside of run()
				checkRun = check.getLastCheckRun();
			}
			accumulator.accumulate(checkRun);
			log.debug("Removing handler from interruptor");
			interruptor.remove(this);
		}
		CheckMDC.remove();
		return checkRun;
	}

	@Override
	public int compareTo(ConcurrentCheckRunHandler o) {
		return Long.compare(this.timeoutEpoch, o.timeoutEpoch);
	}
}
