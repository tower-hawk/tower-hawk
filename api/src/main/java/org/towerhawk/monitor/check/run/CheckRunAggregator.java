package org.towerhawk.monitor.check.run;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface CheckRunAggregator {

	void aggregate(CheckRun.Builder builder, Collection<CheckRun> checkRuns, String succeededMessage, String delimiter);
}
