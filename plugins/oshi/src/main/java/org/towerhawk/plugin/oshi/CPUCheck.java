package org.towerhawk.plugin.oshi;

import lombok.Getter;
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
import oshi.hardware.CentralProcessor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Slf4j
@Extension
@TowerhawkType("cpu")
public class CPUCheck implements CheckExecutor {

	private static CentralProcessor processor;
	private static final Map<String, Function<Object, Object>> defaultResults = new LinkedHashMap<>();
	private static final Map<String, Object> defaultStaticResults = new LinkedHashMap<>();

	static {
		try {
			processor = OSHIUtil.getSystemInfo().getHardware().getProcessor();
			defaultResults.put("systemLoadAverage", f -> processor.getSystemLoadAverage());
			defaultResults.put("systemCpuLoad", f -> processor.getSystemCpuLoad());
			defaultResults.put("systemUptime", f -> processor.getSystemUptime());
			defaultResults.put("systemLoadAverage3", f -> processor.getSystemLoadAverage(3));
			defaultResults.put("processorCpuLoadBetweenTicks", f -> processor.getProcessorCpuLoadBetweenTicks());
			defaultResults.put("processorCpuLoadTicks", f -> processor.getProcessorCpuLoadTicks());
			defaultResults.put("systemCpuLoadTicks", f -> processor.getSystemCpuLoadTicks());

			defaultStaticResults.put("family", processor.getFamily());
			defaultStaticResults.put("model", processor.getModel());
			defaultStaticResults.put("name", processor.getName());
			defaultStaticResults.put("id", processor.getIdentifier());
			defaultStaticResults.put("logicalProcessors", processor.getLogicalProcessorCount());
			defaultStaticResults.put("physicalProcessors", processor.getPhysicalProcessorCount());
			defaultStaticResults.put("stepping", processor.getStepping());
			defaultStaticResults.put("vendor", processor.getVendor());
			defaultStaticResults.put("vendorFreq", processor.getVendorFreq());
		} catch (Throwable t) {
			log.error("Unable to build default results!", t);
		}
	}

	@Setter
	private Set<String> results = new HashSet<>(Arrays.asList("systemLoadAverage", "systemCpuLoad", "systemUptime"));
	private Map<String, Function<Object, Object>> functionMap;
	private Map<String, Object> objectMap;

	@Override
	public ExecutionResult execute(CheckRun.Builder builder, RunContext context) {
		ExecutionResult result = ExecutionResult.of(processor.getSystemLoadAverage());
		functionMap.forEach((key, function) -> result.addResult(key, function.apply(null)));
		objectMap.forEach((key, object) -> result.addResult(key, object));
		return result;
	}

	@Override
	public void init(CheckExecutor checkExecutor, Check check, Config config) {
		functionMap = defaultResults.entrySet().stream()
				.filter(e -> results.contains(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		objectMap = defaultStaticResults.entrySet().stream()
				.filter(e -> results.contains(e.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public void close() throws Exception {

	}
}
