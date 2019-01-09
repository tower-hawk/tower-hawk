package org.towerhawk.monitor.check.execution.constant;

import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.serde.resolver.TowerhawkType;

@Extension
@TowerhawkType("timeout")
public class TimeOutCheck implements CheckExecutor {

	@Override
	public ExecutionResult execute(CheckRun.Builder builder, RunContext runContext) throws Exception {
		builder.message("Sleeping forever");
		Thread.sleep(Long.MAX_VALUE);
		return ExecutionResult.of("Sleeping forever");
	}

	@Override
	public void init(CheckExecutor checkExecutor, Check check, Config config) throws Exception {

	}

	@Override
	public void close() throws Exception {

	}
}
