package org.towerhawk.monitor.app;

import org.pf4j.ExtensionPoint;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.descriptors.Filterable;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.context.RunContext;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

public interface App extends ExtensionPoint, AutoCloseable, Filterable {

	String getId();

	default Long getDefaultCacheMs() {
		return 1000L;
	}

	default Long getDefaultTimeoutMs() {
		return 30000L;
	}

	default Byte getDefaultPriority() {
		return 0;
	}

	default Duration getDefaultAllowedFailureDuration() {
		return Duration.ZERO;
	}

	default Check getCheck(String name) {
		return getChecks().get(name);
	}

	default Set<String> getCheckNames() {
		return getChecks().keySet();
	}

	Map<String, Check> getChecks();

	default CheckRun run(RunContext runContext) {
		return getContainingCheck().run(runContext);
	}

	CheckRun runCheck(String checkId, RunContext runContext);

	Check getContainingCheck();

	void setCheckRunner(CheckRunner runner);

	void init(App previousApp, Config config, String id) throws Exception;
}
