package org.towerhawk.check.app;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.check.AbstractCheck;
import org.towerhawk.check.Check;
import org.towerhawk.check.run.CheckRun;
import org.towerhawk.check.run.CheckRunner;
import org.towerhawk.spring.Configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class App extends AbstractCheck {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	protected Map<String, Check> checks = new LinkedHashMap<>();
	protected long defaultCacheMs;
	protected long defaultTimeoutMs;
	protected long defaultRetryIntervalMs;
	protected long defaultConsecutiveFailures;
	protected int defaultPriority;

	protected CheckRunner checkRunner;

	//TODO add notion of defaults that are separate from settings for app (since an app is a getCheck and can be run)

	public App() {
		//Set some defaults
		Configuration configuration = Configuration.get();
		defaultCacheMs = configuration.getDefaultCacheMs();
		defaultTimeoutMs = configuration.getDefaultTimeoutMs();
		defaultRetryIntervalMs = configuration.getDefaultRetryIntervalMs();
		defaultConsecutiveFailures = configuration.getDefaultConsecutiveFailures();
		defaultPriority = configuration.getDefaultPriority();
		type = "app";
	}

	@Override
	protected void doRun(CheckRun.Builder checkRunBuilder) {
		if (isEnabled()) {
			List<CheckRun> checkRuns = checkRunner.runChecks(checks.values());
			//TODO aggregate checkRuns and add info to builder
			if (checkRuns.stream().anyMatch(r -> r.getStatus() == CheckRun.Status.CRITICAL)) {
				checkRunBuilder.critical();
			} else if (checkRuns.stream().anyMatch(r -> r.getStatus() == CheckRun.Status.WARNING)) {
				checkRunBuilder.warning();
			} else {
				checkRunBuilder.succeeded();
			}
			checkRuns.forEach(checkRun -> checkRunBuilder.addContext(checkRun.getCheck().getId(), checkRun));
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
		if (isEnabled()) {
			log.info("Initialized app {}", getId());
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

	public Map<String, Check> getChecks() {
		return checks;
	}

	public void setCheckRunner(CheckRunner checkRunner) {
		this.checkRunner = checkRunner;
	}

	public long getDefaultCacheMs() {
		return defaultCacheMs;
	}

	public void setDefaultCacheMs(long defaultCacheMs) {
		this.defaultCacheMs = defaultCacheMs;
	}

	public long getDefaultTimeoutMs() {
		return defaultTimeoutMs;
	}

	public void setDefaultTimeoutMs(long defaultTimeoutMs) {
		this.defaultTimeoutMs = defaultTimeoutMs;
	}

	public long getDefaultRetryIntervalMs() {
		return defaultRetryIntervalMs;
	}

	public void setDefaultRetryIntervalMs(long defaultRetryIntervalMs) {
		this.defaultRetryIntervalMs = defaultRetryIntervalMs;
	}

	public long getDefaultConsecutiveFailures() {
		return defaultConsecutiveFailures;
	}

	public void setDefaultConsecutiveFailures(long defaultConsecutiveFailures) {
		this.defaultConsecutiveFailures = defaultConsecutiveFailures;
	}

	public int getDefaultPriority() {
		return defaultPriority;
	}

	public void setDefaultPriority(int defaultPriority) {
		this.defaultPriority = defaultPriority;
	}
}
