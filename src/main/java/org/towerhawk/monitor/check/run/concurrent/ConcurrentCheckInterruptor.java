package org.towerhawk.monitor.check.run.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.spring.config.ConcurrentCheckInterruptorConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Named
public class ConcurrentCheckInterruptor implements Runnable, AutoCloseable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Thread runningThread;
	private PriorityBlockingQueue<ConcurrentCheckRunHandler> checkRunHandlerQueue = new PriorityBlockingQueue<>();
	private boolean running = true;
	private ConcurrentCheckInterruptorConfiguration configuration;
	private ExecutorService interruptorService = Executors.newSingleThreadExecutor();

	@Inject
	public ConcurrentCheckInterruptor(ConcurrentCheckInterruptorConfiguration configuration) {
		this.configuration = configuration;
		this.interruptorService.submit(this);
	}

	private void interrupt() {
		if (log.isTraceEnabled()) {
			log.trace("Interrupting thread {}", runningThread.getName());
		}
		runningThread.interrupt();
	}

	private void submit(ConcurrentCheckRunHandler handler, boolean shouldInterrupt) {
		log.debug("Adding handler for {}", handler.getCheck().getId());
		boolean success = this.checkRunHandlerQueue.add(handler);
		if (success && shouldInterrupt) {
			interrupt();
		}
	}

	public void submit(ConcurrentCheckRunHandler handler) {
		submit(handler, true);
	}

	private void remove(ConcurrentCheckRunHandler handler, boolean shouldInterrupt) {
		log.debug("Removing handler for {}", handler.getCheck().getId());
		boolean success = this.checkRunHandlerQueue.remove(handler);
		if (success && shouldInterrupt) {
			interrupt();
		}
	}

	public void remove(ConcurrentCheckRunHandler handler) {
		remove(handler, true);
	}

	@Override
	public void run() {
		Thread.currentThread().setName(configuration.getThreadName());
		Thread.currentThread().setPriority(configuration.getPriority());
		log.info("Starting {} on thread {} with priority {}", getClass().getSimpleName(), Thread.currentThread().getName(), Thread.currentThread().getPriority());
		while (running) {
			runningThread = Thread.currentThread();
			ConcurrentCheckRunHandler handler = null;
			Future<CheckRun> future = null;
			try {
				log.trace("Polling for checks to timeout");
				handler = checkRunHandlerQueue.poll(configuration.getPollMs(), TimeUnit.MILLISECONDS);

				if (handler != null) {
					future = handler.getCheckRunFuture();
					if (future != null && !future.isDone()) {
						long timeout = handler.getTimeUntilTimeout();
						if (timeout > 0) {
							log.debug("Waiting for {} to run for {} ms", handler.getCheck().getId(), timeout);
							future.get(timeout, TimeUnit.MILLISECONDS);
						} else {
							log.warn("Cancelling {} without waiting", handler.getCheck().getId());
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
				log.warn("Check {} completed exceptionally", handler.getCheck().getId(), e);
				// Do nothing since execution has finished
			} catch (TimeoutException e) {
				//future shouldn't be null since this can only happen when trying to wait for it to finish
				log.info("Cancelling {} after waiting and getting a TimeoutException", handler.getCheck().getId());
				future.cancel(true);
			} catch (Throwable t) {
				log.error("Caught unexpected exception", t);
			}
		}
	}

	@Override
	public void close() throws Exception {
		running = false;
		interrupt();
	}
}
