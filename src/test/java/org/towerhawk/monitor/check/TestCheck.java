package org.towerhawk.monitor.check;

import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.spring.config.Configuration;

import java.util.Collections;

/**
 * This is a class that is meant to be fully functional for testing
 */
public class TestCheck extends AbstractCheck {

	public TestCheck() {
		this("testCheck");
	}

	public TestCheck(String checkId) {
		this("testApp", checkId);
	}

	public TestCheck(String appId, String checkId) {
		App app = new App();
		app.setChecks(Collections.singletonMap(checkId, this));
		app.init(null, new Configuration(), app, appId);
		init(null, app.getConfiguration(), app, checkId);
	}

	@Override
	protected void doRun(CheckRun.Builder builder, RunContext runContext) throws InterruptedException {

	}
}