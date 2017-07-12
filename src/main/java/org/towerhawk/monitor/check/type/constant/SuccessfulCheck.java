package org.towerhawk.monitor.check.type.constant;

import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;

public class SuccessfulCheck extends AbstractCheck {

	@Override
	protected void doRun(CheckRun.Builder builder) throws InterruptedException {
		builder.succeeded().message("Always successful");
	}
}
