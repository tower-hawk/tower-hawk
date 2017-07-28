package org.towerhawk.monitor.check.type;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunAggregator;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.DefaultCheckRunAggregator;
import org.towerhawk.monitor.check.run.ordered.SynchronousCheckRunner;
import org.towerhawk.monitor.check.type.system.LoadAverageCheck;
import org.towerhawk.monitor.check.type.system.PhysicalMemoryCheck;
import org.towerhawk.monitor.check.type.system.SwapSpaceCheck;
import org.towerhawk.serde.resolver.CheckType;
import org.towerhawk.spring.config.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@CheckType("system")
public class SystemCheck extends AbstractCheck {

	private CheckRunAggregator aggregator = new DefaultCheckRunAggregator();
	private CheckRunner checkRunner = new SynchronousCheckRunner();
	private Map<String, Check> checks = new LinkedHashMap<>(3);
	@Getter
	@Setter
	private LoadAverageCheck loadAverage = null;
	@Getter
	@Setter
	private PhysicalMemoryCheck physicalMemory = null;
	@Getter
	@Setter
	private SwapSpaceCheck swapSpace = null;

	public SystemCheck() {
		setCacheMs(0);
	}

	@Override
	protected void doRun(CheckRun.Builder builder, RunContext runContext) throws InterruptedException {
		List<CheckRun> checkRuns = checkRunner.runChecks(checks.values(), runContext.duplicate());
		aggregator.aggregate(builder, checkRuns, "OK", getConfiguration().getLineDelimiter());
		checkRuns.stream().forEachOrdered(c -> {
			Map<String, Object> context = c.getContext();
			if (context != null) {
				context.entrySet().stream().forEachOrdered(e -> builder.addContext(e.getKey(), e.getValue()));
			}
		});
	}

	@Override
	public void init(Check check, Configuration configuration, App app, String id) {
		super.init(check, configuration, app, id);
		if (loadAverage == null) {
			loadAverage = new LoadAverageCheck();
		}
		if (physicalMemory == null) {
			physicalMemory = new PhysicalMemoryCheck();
		}
		if (swapSpace == null) {
			swapSpace = new SwapSpaceCheck();
		}
		checks.put("loadAverage", loadAverage);
		checks.put("physicalMemory", physicalMemory);
		checks.put("swapSpace", swapSpace);
		checks.forEach((k, v) -> {
			Check c = null;
			if (check instanceof SystemCheck) {
				c = ((SystemCheck) check).checks.get(k);
			}
			v.init(c, configuration, getApp(), getId() + "-" + k);
		});
	}
}
