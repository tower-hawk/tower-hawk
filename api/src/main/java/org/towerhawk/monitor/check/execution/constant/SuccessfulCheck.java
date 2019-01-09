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
@TowerhawkType("success")
public class SuccessfulCheck implements CheckExecutor {

	@Override
	public ExecutionResult execute(CheckRun.Builder builder, RunContext runContext) throws Exception {
		builder.succeeded().message("Always successful");
		return ExecutionResult.of("Always successful");
	}

	@Override
	public void init(CheckExecutor checkExecutor, Check check, Config config) throws Exception {

	}

	@Override
	public void close() throws Exception {

	}
}
