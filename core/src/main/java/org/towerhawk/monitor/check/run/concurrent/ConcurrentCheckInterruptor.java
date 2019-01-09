package org.towerhawk.monitor.check.run.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.logging.CheckMDC;
import org.towerhawk.monitor.check.run.CheckRun;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;

@Named
@Slf4j
public class ConcurrentCheckInterruptor implements Runnable, AutoCloseable {

	private Thread runningThread;
	private PriorityBlockingQueue<ConcurrentCheckRunHandler> checkRunHandlerQueue = new PriorityBlockingQueue<>();
	private boolean running = true;
	private String threadName;
	private int priority;
	private long pollMs;
	private ExecutorService interruptorService = Executors.newSingleThreadExecutor();

	@Inject
	public ConcurrentCheckInterruptor(Config config) {
		threadName = config.getString("threadName", "ConcurrentCheckInterruptorThread");
		priority = config.getInt("priority", 6);
		pollMs = config.getLong("pollMs", 10000L);
		this.interruptorService.submit(this);
	}

	private void interrupt() {
		if (log.isTraceEnabled()) {
			log.trace("Interrupting thread {}", runningThread.getName());
		}
		runningThread.interrupt();
	}

	private void submit(ConcurrentCheckRunHandler handler, boolean shouldInterrupt) {
		log.debug("Adding handler for check");
		boolean success = this.checkRunHandlerQueue.add(handler);
		if (success && shouldInterrupt) {
			interrupt();
		}
	}

	public void submit(ConcurrentCheckRunHandler handler) {
		submit(handler, true);
	}

	public boolean remove(ConcurrentCheckRunHandler handler) {
		log.debug("Removing handler for check");
		return this.checkRunHandlerQueue.remove(handler);
	}

	@Override
	public void run() {
		Thread.currentThread().setName(threadName);
		Thread.currentThread().setPriority(priority);
		log.info("Starting {} on thread {} with priority {}", getClass().getSimpleName(), Thread.currentThread().getName(), Thread.currentThread().getPriority());
		while (running) {
			runningThread = Thread.currentThread();
			ConcurrentCheckRunHandler handler = null;
			Future<CheckRun> future = null;
			try {
				log.trace("Polling for checks to timeout");
				handler = checkRunHandlerQueue.poll(pollMs, TimeUnit.MILLISECONDS);

				if (handler != null) {
					CheckMDC.put(handler.getCheck());
					future = handler.getCheckRunFuture();
					if (future != null && !future.isDone()) {
						long timeout = handler.getTimeUntilTimeout();
						if (timeout > 0) {
							log.debug("Waiting for check to run for {} ms", timeout);
							future.get(timeout, TimeUnit.MILLISECONDS);
						} else {
							log.warn("Cancelling check without waiting");
							future.cancel(true);
						}
					}
				}
			} catch (InterruptedException e) {
				if (log.isTraceEnabled()) {
					log.trace("Got interrupted on {}", Thread.currentThread().getName());
				}
				// either closing down or having an object added or removed so just loop again
				if (handler != null) {
					submit(handler, false);
				}
			} catch (ExecutionException e) {
				log.warn("Check completed exceptionally", e);
				// Do nothing since execution has finished
			} catch (TimeoutException e) {
				//future shouldn't be null since this can only happen when trying to wait for it to finish
				log.info("Cancelling after waiting and getting a TimeoutException");
				future.cancel(true);
			} catch (CancellationException e) {
				log.warn("Check was cancelled while waiting on it");
			} catch (Throwable t) {
				if (handler != null) {
					handler.cancel();
				}
				log.error("Caught unexpected exception for check", t);
			} finally {
				CheckMDC.clear();
			}
		}
	}

	@Override
	public void close() throws Exception {
		running = false;
		interrupt();
	}
}
