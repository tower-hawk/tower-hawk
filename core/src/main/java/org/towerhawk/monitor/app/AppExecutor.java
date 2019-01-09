package org.towerhawk.monitor.app;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.filter.CheckFilter;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.context.RunContext;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@Extension
public class AppExecutor implements CheckExecutor {

	protected App app;
	protected CheckRunner checkRunner;
	protected String predicateKey;

	public AppExecutor(App app, CheckRunner checkRunner, String predicateKey) {
		this.app = app;
		this.checkRunner = checkRunner;
		this.predicateKey = predicateKey;
	}

	@Override
	public void init(CheckExecutor executor, Check check, Config config) throws Exception {

	}

	@Override
	public ExecutionResult execute(CheckRun.Builder builder, RunContext runContext) throws Exception {
		ExecutionResult result = ExecutionResult.startTimer();
		Object mapPredicate = runContext.getContext().get(predicateKey);
		Collection<Check> checksToRun;
		if (mapPredicate instanceof CheckFilter) {
			checksToRun = app.getChecks().values().stream().filter(((CheckFilter) mapPredicate)::filter).collect(Collectors.toList());
			runContext.getContext().remove(predicateKey);
			if (checksToRun.size() != app.getChecks().size()) {
				runContext.setSaveCheckRun(false);
			}
		} else {
			checksToRun = app.getChecks().values();
		}
		RunContext context = runContext.duplicate().setSaveCheckRun(true);
		List<CheckRun> checkRuns = checkRunner.runChecks(checksToRun, context);
		result.complete();
		checkRuns.forEach(checkRun -> result.addResult(checkRun.getCheck().getId(), checkRun));
		return result;
	}

	@Override
	public void close() {
		app.getChecks().values().forEach(c -> {
			try {
				c.close();
			} catch (Exception e) {
				log.error("Check {} failed to close with exception", c.getFullName(), e);
			}
		});
	}
}
