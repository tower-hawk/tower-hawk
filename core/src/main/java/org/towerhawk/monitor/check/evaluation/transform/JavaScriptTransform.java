package org.towerhawk.monitor.check.evaluation.transform;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.scripting.NashornScriptEvaluator;


@Setter
@Getter
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class JavaScriptTransform implements Transform {

	private NashornScriptEvaluator evaluator;

	@JsonCreator
	public JavaScriptTransform(
			@JsonProperty("name") String name,
			@JsonProperty("function") String function,
			@JsonProperty("script") String script,
			@JsonProperty("file") String file
	) {
		if (name == null || name.isEmpty()) {
			name = "jsTransform";
		}
		if (function == null || function.isEmpty()) {
			function = "transform";
		}
		evaluator = new NashornScriptEvaluator(name, function, script, file);
	}

	@Override
	public Object transform(Object value) throws Exception {
		return evaluator.invoke(value);
	}
}

