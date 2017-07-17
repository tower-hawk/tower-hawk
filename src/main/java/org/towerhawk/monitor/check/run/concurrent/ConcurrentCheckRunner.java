package org.towerhawk.monitor.check.run.concurrent;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.CheckContext;
import org.towerhawk.monitor.check.run.CheckRun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ConcurrentCheckRunner implements AsynchronousCheckRunner {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Getter
	private final ExecutorService checkRunService;
	@Getter
	private final ConcurrentCheckInterruptor interruptor;

	public ConcurrentCheckRunner(ConcurrentCheckInterruptor interruptor, ExecutorService checkRunService) {
		this.interruptor = interruptor;
		this.checkRunService = checkRunService;
	}

	@Override
	public ConcurrentCheckRunAccumulator runChecksAsync(Collection<Check> checks, CheckContext checkContext) {
		List<Check> checkList = new ArrayList<>(checks);
		Collections.sort(checkList);
		ConcurrentCheckRunAccumulator accumulator = new ConcurrentCheckRunAccumulator(checkList);
		log.debug("Building handlers");
		Collection<ConcurrentCheckRunHandler> handlers = checkList.stream().map(c -> new ConcurrentCheckRunHandler(c, accumulator, interruptor, checkContext)).collect(Collectors.toList());
		handlers.forEach(h -> {
			if (h.getCheck().canRun()) {
				log.debug("Submitting handler for {}", h.getCheck().getFullName());
				Future<CheckRun> handlerFuture = checkRunService.submit(h);
				h.setCheckRunFuture(handlerFuture);
				accumulator.addHandler(h);
			} else { //Shortcut calling the shouldRun method just to get a cached result
				accumulator.accumulate(h.getCheck().getLastCheckRun());
			}
		});
		log.debug("Returning accumulator");
		return accumulator;
	}

	@Override
	public List<CheckRun> runChecks(Collection<Check> checks, CheckContext checkContext) {
		ConcurrentCheckRunAccumulator accumulator = runChecksAsync(checks, checkContext);
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

