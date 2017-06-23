package org.towerhawk.check.app;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.check.AbstractCheck;
import org.towerhawk.check.Check;
import org.towerhawk.check.run.CheckRun;
import org.towerhawk.check.run.CheckRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class App extends AbstractCheck {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	protected Map<String, Check> checks = new LinkedHashMap<>();
	protected boolean enabled = true;

	protected CheckRunner checkRunner;

	//TODO add notion of defaults that are separate from settings for app (since an app is a check and can be run)

	public App() {
		//Set some defaults
		cacheMs = 30000;
		timeoutMs = 60000;
		retryIntervalMs = 60000;
		consecutiveFailures = 1;
		priority = 20;
		type = "app";
	}

	public Map<String, Check> getChecks() {
		return checks;
	}

	public void setCheckRunner(CheckRunner checkRunner) {
		this.checkRunner = checkRunner;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	protected void doRun(CheckRun.Builder checkRunBuilder) {
		if (enabled) {
			List<CheckRun> checkRuns = checkRunner.runChecks(checks.values());
			//TODO aggregate checkRuns and add info to builder
			if (checkRuns.stream().anyMatch(r -> r.status() == CheckRun.Status.CRITICAL)) {
				checkRunBuilder.critical();
			} else if (checkRuns.stream().anyMatch(r -> r.status() == CheckRun.Status.WARNING)) {
				checkRunBuilder.warning();
			} else {
				checkRunBuilder.succeeded();
			}
			checkRuns.forEach(checkRun -> checkRunBuilder.addContext(checkRun.check().getId(), checkRun));
		}
	}

	@Override
	public void init(Check check) {
		super.init(check);
		App previousApp = (App) check;
		getChecks().forEach((id, c) -> {
			c.setId(id);
			c.setApp(this);
			c.init(previousApp == null ? null : previousApp.getChecks().get(c.getId()));
		});
		checks = Collections.unmodifiableMap(checks);
		if (enabled) {
			log.info("Initialized app {}", id);
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		checks.values().stream().forEach(c -> {
			try {
				c.close();
			} catch (IOException e) {
				log.error("Check {} failed to close with error", c.getId(), e);
			}
		});
	}
}
