package org.towerhawk.monitor.check.evaluation;

import org.pf4j.ExtensionPoint;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;

public interface Evaluator extends ExtensionPoint {

	void evaluate(CheckRun.Builder builder, String key, ExecutionResult result) throws Exception;
}
