package org.towerhawk.monitor.descriptors;

import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;

public interface Executable {

	/**
	 * This allows an execution to be called directly or to be handed to a new check.
	 *
	 * @return The execution that this check is using
	 */
	CheckExecutor getExecutor();

	void setExecutor(CheckExecutor executor);

	default ExecutionResult execute(CheckRun.Builder builder, RunContext context) throws Exception {
		return getExecutor().execute(builder, context);
	}

}
