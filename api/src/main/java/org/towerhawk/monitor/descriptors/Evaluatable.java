package org.towerhawk.monitor.descriptors;

import org.towerhawk.monitor.check.evaluation.Evaluator;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;

public interface Evaluatable {


	Evaluator getEvaluator();

	default void evaluate(CheckRun.Builder builder, String key, ExecutionResult result) throws Exception {
		getEvaluator().evaluate(builder, key, result);
	}

	void setEvaluator(Evaluator evaluator);
}
