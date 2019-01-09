package org.towerhawk.monitor.app;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.active.Active;
import org.towerhawk.monitor.active.Enabled;
import org.towerhawk.monitor.check.cluster.Cluster;
import org.towerhawk.monitor.check.cluster.NoCluster;
import org.towerhawk.monitor.descriptors.Activatable;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.DefaultCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.monitor.check.run.ordered.SynchronousCheckRunner;
import org.towerhawk.monitor.descriptors.Clusterable;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.time.Duration;
import java.util.*;

@Slf4j
@Getter
@Setter
@Extension
@TowerhawkType("default")
public class DefaultApp implements App, Activatable, Clusterable {

	protected DefaultCheck settings = new DefaultCheck();
	protected Map<String, Check> checks = new LinkedHashMap<>();
	protected Long defaultCacheMs;
	protected Long defaultTimeoutMs;
	protected Byte defaultPriority;
	protected Duration defaultAllowedFailureDuration;
	protected CheckRunner checkRunner;
	protected Config config;
	protected String id;

	public DefaultApp() {
		this(null);
	}

	public DefaultApp(Map<String, Check> checks) {
		if (checks != null && !checks.isEmpty()) {
			this.checks.putAll(checks);
		}
	}

	public void setDefaultAllowedFailureDurationMs(long defaultAllowedFailureDurationMs) {
		defaultAllowedFailureDuration = Duration.ofMillis(defaultAllowedFailureDurationMs);
	}

	public String predicateKey() {
		return "predicate";
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getFullName() {
		return getContainingCheck().getFullName();
	}

	@Override
	public String getAlias() {
		return getContainingCheck().getAlias();
	}

	@Override
	public String getType() {
		return getContainingCheck().getType();
	}

	@Override
	public Set<String> getTags() {
		return getContainingCheck().getTags();
	}

	public Check getCheck(String checkId) {
		return getChecks().get(checkId);
	}

	public Set<String> getCheckNames() {
		return getChecks().keySet();
	}

	@Override
	public CheckRun runCheck(String checkId, RunContext runContext) {
		List<CheckRun> checkRuns = checkRunner.runChecks(Collections.singletonList(checks.get(checkId)), runContext);
		return checkRuns.get(0);
	}

	@Override
	public Check getContainingCheck() {
		return settings;
	}

	@Override
	public void init(App previousApp, Config config, String id) throws Exception {
		this.id = id;
		this.config = config;
		if (checks == null) {
			throw new IllegalStateException("App " + getId() + " must have at least one check");
		}

		if (defaultCacheMs == null) {
			defaultCacheMs = config.getLong("defaultCacheMs", 0L);
		}
		if (defaultTimeoutMs == null) {
			defaultTimeoutMs = config.getLong("defaultTimeoutMs", 10000L);
		}
		if (defaultPriority == null) {
			defaultPriority = config.getByte("defaultPriority", (byte) 0);
		}
		if (defaultAllowedFailureDuration == null) {
			defaultAllowedFailureDuration = Duration.ofMillis(config.getLong("defaultAllowedFailureDurationMs", 0L));
		}

		Check previousCheck = null;
		if (previousApp instanceof DefaultApp) {
			previousCheck = ((DefaultApp) previousApp).getContainingCheck();
		}
		settings.setEvaluator(new AppEvaluator());
		settings.setExecutor(new AppExecutor(this, checkRunner == null ? new SynchronousCheckRunner() : checkRunner, predicateKey()));
		settings.init(previousCheck, config, this, "defaultApp");

		//Must go at the end of app initialization
		getChecks().forEach((checkId, c) -> {
			try {
				c.init(previousApp == null ? null : previousApp.getCheck(checkId), config, this, checkId);
			} catch (Exception e) {
				log.error("Check {} in app {} errored during initalization", checkId, getId(), e);
			}
		});
		checks = Collections.unmodifiableMap(getChecks());
	}

	@Override
	public void close() throws Exception {
		for (Check check : checks.values()) {
			try {
				check.close();
			} catch (Exception e) {
				log.warn("Unable to close check {}", check.getFullName());
			}
		}
	}

	@Override
	public Active getActive() {
		return settings.getActive();
	}

	@Override
	public void setActive(Active active) {
		settings.setActive(active);
	}

	@Override
	public Cluster getCluster() {
		return settings.getCluster();
	}

	@Override
	public void setCluster(Cluster cluster) {
		settings.setCluster(cluster);
	}
}
