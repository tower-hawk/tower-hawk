package org.towerhawk.monitor.check;

import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.spring.config.Configuration;

/**
 * This is a class that is meant to be fully functional for testing
 */
public class TestCheck extends AbstractCheck {

	public TestCheck() {
		init(null, new Configuration(), new App(), "testCheck");
	}

	@Override
	protected void doRun(CheckRun.Builder builder, CheckContext checkContext) throws InterruptedException {

	}
}