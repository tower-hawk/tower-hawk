package org.towerhawk.monitor.check.type.constant;

import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.serde.resolver.CheckType;

@CheckType("warning")
public class WarningCheck extends AbstractCheck {

	@Override
	protected void doRun(CheckRun.Builder builder, RunContext runContext) throws InterruptedException {
		builder.warning().message("Always warning");
	}
}
