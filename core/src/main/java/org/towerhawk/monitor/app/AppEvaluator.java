package org.towerhawk.monitor.app;

import org.pf4j.Extension;
import org.towerhawk.monitor.check.evaluation.Evaluator;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunAggregator;

import java.util.List;

@Extension
public class AppEvaluator implements Evaluator {

	protected String delimiter = ",";
	protected CheckRunAggregator aggregator;

	@Override
	public void evaluate(CheckRun.Builder builder, String key, ExecutionResult result) {

	}

	protected void aggregateChecks(CheckRun.Builder builder, List<CheckRun> checkRuns) {
		aggregator.aggregate(builder, checkRuns, "OK", delimiter);
		checkRuns.forEach(checkRun -> builder.addContext(checkRun.getCheck().getId(), checkRun));
	}
}
