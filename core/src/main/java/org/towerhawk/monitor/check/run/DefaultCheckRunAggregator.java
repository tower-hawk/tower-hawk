package org.towerhawk.monitor.check.run;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultCheckRunAggregator implements CheckRunAggregator {

	public void aggregate(CheckRun.Builder builder, Collection<CheckRun> checkRuns, String succeededMessage, String delimiter) {
		List<CheckRun> filteredCheckRuns;
		if ((filteredCheckRuns = checkRuns.stream().filter(r -> r.getStatus() == Status.CRITICAL).collect(Collectors.toList())).size() > 0) {
			builder.critical();
		} else if ((filteredCheckRuns = checkRuns.stream().filter(r -> r.getStatus() == Status.WARNING).collect(Collectors.toList())).size() > 0) {
			builder.warning();
		} else {
			builder.succeeded();
			builder.message(succeededMessage);
		}
		if (!filteredCheckRuns.isEmpty()) {
			builder.message(
					filteredCheckRuns.stream()
							.filter(r -> r.getMessage() != null && !r.getMessage().isEmpty())
							.sorted()
							.map(checkRun -> checkRun.getCheck().getId() + ": " + checkRun.getMessage())
							.collect(Collectors.joining(delimiter)));
		}
	}
}
