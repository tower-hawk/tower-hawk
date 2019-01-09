package org.towerhawk.plugin.oshi;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.plugin.oshi.util.OSHIUtil;
import org.towerhawk.serde.resolver.TowerhawkType;
import oshi.hardware.GlobalMemory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Extension
@TowerhawkType("memory")
public class MemoryCheck implements CheckExecutor {

	private static GlobalMemory memory;
	private static final Map<String, Function<Object, Object>> defaultResults = new LinkedHashMap<>();

	static {
		try {
			memory = OSHIUtil.getSystemInfo().getHardware().getMemory();
			defaultResults.put("total", f -> memory.getTotal());
			defaultResults.put("available", f -> memory.getAvailable());
			defaultResults.put("used", f -> memory.getTotal() - memory.getAvailable());
			defaultResults.put("percentUsed", f -> 100 - (memory.getAvailable() / memory.getTotal() * 100));
			defaultResults.put("percentAvailable", f -> memory.getAvailable() / memory.getTotal() * 100);
			defaultResults.put("swapTotal", f -> memory.getSwapTotal());
			defaultResults.put("swapUsed", f -> memory.getSwapUsed());
			defaultResults.put("swapAvailable", f -> memory.getSwapTotal() - memory.getSwapUsed());
			defaultResults.put("percentSwapUsed", f -> memory.getSwapUsed() / memory.getSwapTotal() * 100);
			defaultResults.put("percentSwapAvailable", f -> 1 - (memory.getSwapUsed() / memory.getSwapTotal() * 100));
		} catch (Throwable t) {
			log.error("Unable to build default results!", t);
		}
	}

	@Setter
	private Set<String> results = new HashSet<>(Arrays.asList("available", "percentAvailable", "swapAvailable", "percentSwapAvailable"));
	private Map<String, Function<Object, Object>> functionMap;

	@Override
	public void init(CheckExecutor checkExecutor, Check check, Config config) throws Exception {
		functionMap = defaultResults.entrySet().stream()
				.filter(e -> results.contains(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public ExecutionResult execute(CheckRun.Builder builder, RunContext context) throws Exception {
		ExecutionResult result = ExecutionResult.startTimer();
		functionMap.forEach((key, function) -> result.addResult(key, function.apply(null)));
		result.complete();
		return result;
	}

	@Override
	public void close() throws Exception {

	}
}
