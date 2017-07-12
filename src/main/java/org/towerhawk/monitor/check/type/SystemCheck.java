package org.towerhawk.monitor.check.type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunAggregator;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.DefaultCheckRunAggregator;
import org.towerhawk.monitor.check.run.ordered.SynchronousCheckRunner;
import org.towerhawk.monitor.check.type.system.LoadAverage;
import org.towerhawk.monitor.check.type.system.PhysicalMemory;
import org.towerhawk.monitor.check.type.system.SwapSpace;
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
	@Getter	@Setter	private InternalLoadAverage loadAverage = null;
	@Getter	@Setter	private InternalPhysicalMemory physicalMemory = null;
	@Getter	@Setter	private InternalSwapSpace swapSpace = null;

	public SystemCheck() {
		cacheMs = 0;
	}

	@Override
	protected void doRun(CheckRun.Builder builder) throws InterruptedException {
		List<CheckRun> checkRuns = checkRunner.runChecks(checks.values());
		aggregator.aggregate(builder, checkRuns, "OK", configuration.getLineDelimiter());
		checkRuns.forEach(c -> {
			Map<String, Object> context = c.getContext();
			if (context != null) {
				context.forEach((k, v) -> builder.addContext(k, v));
			}
		});
	}

	@Override
	public void init(Check check, Configuration configuration, App app, String id) {
		super.init(check, configuration, app, id);
		if (loadAverage == null) {
			loadAverage = new InternalLoadAverage();
		}
		if (physicalMemory == null) {
			physicalMemory = new InternalPhysicalMemory();
		}
		if (swapSpace == null) {
			swapSpace = new InternalSwapSpace();
		}
		checks.put("loadAverage", loadAverage);
		checks.put("physicalMemory", physicalMemory);
		checks.put("swapSpace", swapSpace);
		checks.forEach((k, v) -> {
			Check c = null;
			if (check instanceof SystemCheck) {
				c = ((SystemCheck)check).checks.get(k);
			}
			v.init(c, configuration, getApp(), getId() + "-" + k);
		});
	}

}

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
class InternalLoadAverage extends LoadAverage {
	//Nothing to change except the annotation so type isn't required in the config yaml
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
class InternalPhysicalMemory extends PhysicalMemory {
	//Nothing to change except the annotation so type isn't required in the config yaml
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
class InternalSwapSpace extends SwapSpace {
	//Nothing to change except the annotation so type isn't required in the config yaml
}