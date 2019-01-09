package org.towerhawk.monitor.check;

import lombok.extern.slf4j.Slf4j;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.app.DefaultApp;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.spring.config.Configuration;

import java.util.Collections;

/**
 * This is a class that is meant to be fully functional for testing
 */
@Slf4j
public class TestCheck extends DefaultCheck {

	public TestCheck() {
		this("testCheck");
	}

	public TestCheck(String checkId) {
		this("testApp", checkId);
	}

	public TestCheck(String appId, String checkId) {
		DefaultApp app = new DefaultApp();
		this.setExecutor(new CheckExecutor() {
			@Override
			public void init(CheckExecutor checkExecutor, Check check, Config config) throws Exception {

			}

			@Override
			public ExecutionResult execute(CheckRun.Builder builder, RunContext context) throws Exception {
				return ExecutionResult.of("test");
			}

			@Override
			public void close() throws Exception {

			}
		});
		Configuration configuration = new Configuration();
		app.setChecks(Collections.singletonMap(checkId, this));
		try {
			app.init(null, configuration, appId);
		} catch (Exception e) {
			log.error("Unable to initalize TestCheck app", e);
		}
		try {
			init(null, app.getConfig(), app, checkId);
		} catch (Exception e) {
			log.error("Unable to initialize TestCheck");
		}
	}
}