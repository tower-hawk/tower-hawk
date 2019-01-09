package org.towerhawk.monitor.check.execution.script;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.scripting.NashornScriptEvaluator;

import java.util.Map;

@Getter
@Setter
@Slf4j
public class JavaScriptCheck implements CheckExecutor {

	private NashornScriptEvaluator evaluator;

	@JsonCreator
	public JavaScriptCheck(
			@JsonProperty("name") String name,
			@JsonProperty("function") String function,
			@JsonProperty("script") String script,
			@JsonProperty("file") String file
	) {
		if (name == null || name.isEmpty()) {
			name = "jsCheck";
		}
		if (function == null || function.isEmpty()) {
			function = "run";
		}
		evaluator = new NashornScriptEvaluator(name, function, script, file);
	}

	@Override
	public ExecutionResult execute(CheckRun.Builder builder, RunContext context) throws Exception {
		ExecutionResult result = ExecutionResult.startTimer();
		Object invokeResult = evaluator.invoke(builder, context);
		result.complete();
		if (invokeResult instanceof Map) {
			((Map<?, ?>) invokeResult).forEach((k, v) -> result.addResult(k.toString(), v));
		} else {
			result.setResult(invokeResult);
		}
		return result;
	}

	@Override
	public void init(CheckExecutor checkExecutor, Check check, Config config) throws Exception {

	}

	@Override
	public void close() throws Exception {

	}
}
