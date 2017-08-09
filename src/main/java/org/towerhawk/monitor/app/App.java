package org.towerhawk.monitor.app;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.filter.CheckFilter;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunAggregator;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.DefaultCheckRunAggregator;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.spring.config.Configuration;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE) //remove the need to specify a type
public class App extends AbstractCheck {

	protected CheckRunAggregator aggregator = new DefaultCheckRunAggregator();
	protected Map<String, Check> checks = null;
	@Getter
	@Setter
	protected long defaultCacheMs;
	@Getter
	@Setter
	protected long defaultTimeoutMs;
	@Getter
	@Setter
	protected byte defaultPriority;
	@Setter
	protected CheckRunner checkRunner;

	public App() {
		this(null);
	}

	public App(Map<String, Check> checks) {
		this.checks = checks;
		setType("app");
	}

	public String predicateKey() {
		return "predicate";
	}

	public Check getCheck(String checkId) {
		return getChecks().get(checkId);
	}

	protected Map<String, Check> getChecks() {
		return checks;
	}

	protected void setChecks(Map<String, Check> checks) {
		this.checks = checks;
	}

	public Collection<String> getCheckNames() {
		return getChecks().keySet();
	}

	@Override
	protected void doRun(CheckRun.Builder builder, RunContext runContext) {
		Object mapPredicate = runContext.getContext().get(predicateKey());
		Collection<Check> checksToRun;
		if (mapPredicate instanceof CheckFilter) {
			checksToRun = getChecks().values().stream().filter(((CheckFilter) mapPredicate)::filter).collect(Collectors.toList());
			runContext.getContext().remove(predicateKey());
			if (checksToRun.size() != getChecks().size()) {
				runContext.setSaveCheckRun(false);
			}
		} else {
			checksToRun = getChecks().values();
		}
		RunContext context = runContext.duplicate().setSaveCheckRun(true);
		List<CheckRun> checkRuns = checkRunner.runChecks(checksToRun, context);
		aggregateChecks(builder, checkRuns);
	}

	protected void aggregateChecks(CheckRun.Builder builder, List<CheckRun> checkRuns) {
		aggregator.aggregate(builder, checkRuns, "OK", getConfiguration().getLineDelimiter());
		checkRuns.forEach(checkRun -> builder.addContext(checkRun.getCheck().getId(), checkRun));
	}

	@Override
	public void init(Check check, Configuration configuration, App app, String id) {
		if (checks == null) {
			throw new IllegalStateException("App " + id + " must have at least one check");
		}
		super.init(check, configuration, app, id);
		defaultCacheMs = getConfiguration().getDefaultCacheMs();
		defaultTimeoutMs = getConfiguration().getDefaultTimeoutMs();
		defaultPriority = getConfiguration().getDefaultPriority();
		App previousApp = (App) check;
		getChecks().forEach((checkId, c) -> c.init(previousApp == null ? null : previousApp.getCheck(checkId), configuration, this, checkId));
		checks = Collections.unmodifiableMap(getChecks());
		//an App should never be cached so override any cache settings
		setCacheMs(0);
		fullName = "app:" + getId();
		if (isActive()) {
			log.info("Initialized {}", getFullName());
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		getChecks().values().forEach(c -> {
			try {
				c.close();
			} catch (IOException e) {
				log.error("Check {} failed to close with exception", c.getFullName(), e);
			}
		});
	}
}
