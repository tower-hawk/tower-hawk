package org.towerhawk.monitor.check.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunAggregator;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.ordered.SynchronousCheckRunner;
import org.towerhawk.monitor.check.type.system.LoadAverage;
import org.towerhawk.monitor.check.type.system.PhysicalMemory;
import org.towerhawk.monitor.check.type.system.SwapSpace;
import org.towerhawk.spring.config.Configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class System extends AbstractCheck {

	private static Logger log = LoggerFactory.getLogger(System.class);
	private CheckRunAggregator aggregator = new CheckRunAggregator() {};
	private CheckRunner checkRunner = new SynchronousCheckRunner();
	private Map<String, Check> checks = new LinkedHashMap<>(3);
	private LoadAverage loadAverage = new LoadAverage();
	private PhysicalMemory physicalmemory = new PhysicalMemory();
	private SwapSpace swapSpace = new SwapSpace();

	public System() {
		cacheMs = 2000;
		checks.put("loadAverage", loadAverage);
		checks.put("physicalMemory", physicalmemory);
		checks.put("swapSpace", swapSpace);
	}

	@Override
	protected void doRun(CheckRun.Builder builder) throws InterruptedException {
		List<CheckRun> checkRuns = checkRunner.runChecks(checks.values());
		aggregator.aggregate(builder, checkRuns, "OK", configuration.getLineDelimiter());
		checkRuns.forEach(c -> c.getContext().forEach((k, v) -> builder.addContext(k, v)));
	}

	@Override
	public void init(Check check, Configuration configuration) {
		super.init(check, configuration);
		checks.forEach((k, v) -> {
			v.setId(getId() + "-" + k);
			v.setApp(getApp());
			if (check instanceof System) {
				System system = (System)check;
				v.init(system.checks.get(k), configuration);
			} else {
				v.init(null, configuration);
			}
		});
	}

	public LoadAverage getLoadAverage() {
		return loadAverage;
	}

	public void setLoadAverage(LoadAverage loadAverage) {
		this.loadAverage = loadAverage;
	}

	public PhysicalMemory getPhysicalmemory() {
		return physicalmemory;
	}

	public void setPhysicalmemory(PhysicalMemory physicalmemory) {
		this.physicalmemory = physicalmemory;
	}

	public SwapSpace getSwapSpace() {
		return swapSpace;
	}

	public void setSwapSpace(SwapSpace swapSpace) {
		this.swapSpace = swapSpace;
	}
}