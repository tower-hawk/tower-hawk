package org.towerhawk.monitor.check.evaluation.threshold;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.scripting.NashornScriptEvaluator;
import org.towerhawk.serde.resolver.TowerhawkType;

@Getter
@Setter
@Slf4j
@Extension
@TowerhawkType({"js","javaScript"})
public class JavaScriptThreshold implements Threshold {

	private NashornScriptEvaluator evaluator;

	@JsonCreator
	public JavaScriptThreshold(
			@JsonProperty("name") String name,
			@JsonProperty("function") String function,
			@JsonProperty("script") String script,
			@JsonProperty("file") String file
	) {
		if (name == null || name.isEmpty()) {
			name = "jsThreshold";
		}
		if (function == null || function.isEmpty()) {
			function = "evaluate";
		}
		evaluator = new NashornScriptEvaluator(name, function, script, file);
	}

	@Override
	public void evaluate(CheckRun.Builder builder, String key, Object value, boolean setMessage, boolean addContext) throws Exception {
		try {
			evaluator.getEngine().invokeFunction(evaluator.getFunction(), builder, value, addContext, setMessage);
		} catch (Exception e) {
			log.warn("Caught exception while executing evaluation for " + evaluator.getName(), e);
			builder.critical().error(e);
		}
	}
}
