package org.towerhawk.monitor.check.type.constant;

import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.serde.resolver.CheckType;

@CheckType("critical")
public class CriticalCheck extends AbstractCheck {

	@Override
	protected void doRun(CheckRun.Builder builder, RunContext runContext) throws InterruptedException {
		builder.critical().message("Always critical");
	}
}
