package org.towerhawk.monitor.app;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunAggregator;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.DefaultCheckRunAggregator;
import org.towerhawk.spring.config.Configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class App extends AbstractCheck {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	protected CheckRunAggregator aggregator = new DefaultCheckRunAggregator();
	protected Map<String, Check> checks = new LinkedHashMap<>();
	protected long defaultCacheMs;
	protected long defaultTimeoutMs;
	protected int defaultPriority;

	protected CheckRunner checkRunner;

	public App() {
		type = "app";
		cacheMs = 0;
	}

	@Override
	protected void doRun(CheckRun.Builder builder) {
		List<CheckRun> checkRuns = checkRunner.runChecks(checks.values());
		aggregator.aggregate(builder, checkRuns, "OK", configuration.getLineDelimiter());
		checkRuns.forEach(checkRun -> builder.addContext(checkRun.getCheck().getId(), checkRun));
	}

	@Override
	public void init(Check check, Configuration configuration, App app, String id) {
		super.init(check, configuration, app, id);
		defaultCacheMs = configuration.getDefaultCacheMs();
		defaultTimeoutMs = configuration.getDefaultTimeoutMs();
		defaultPriority = configuration.getDefaultPriority();
		App previousApp = (App) check;
		getChecks().forEach((checkId, c) -> {
			c.init(previousApp == null ? null : previousApp.getChecks().get(c.getId()), configuration, this, checkId);
		});
		checks = Collections.unmodifiableMap(checks);
		if (isActive()) {
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

	public int getDefaultPriority() {
		return defaultPriority;
	}

	public void setDefaultPriority(int defaultPriority) {
		this.defaultPriority = defaultPriority;
	}
}