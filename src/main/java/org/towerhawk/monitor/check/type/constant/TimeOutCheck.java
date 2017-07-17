package org.towerhawk.monitor.check.type.constant;

import org.towerhawk.monitor.check.CheckContext;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.serde.resolver.CheckType;

@CheckType("timeout")
public class TimeOutCheck extends AbstractCheck {

	@Override
	protected void doRun(CheckRun.Builder builder, CheckContext checkContext) throws InterruptedException {
		builder.message("Sleeping forever");
		Thread.sleep(Long.MAX_VALUE);
	}
}
