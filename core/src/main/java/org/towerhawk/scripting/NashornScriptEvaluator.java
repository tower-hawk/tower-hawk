package org.towerhawk.scripting;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.script.ScriptException;

@Getter
@Slf4j
public class NashornScriptEvaluator extends AbstractScriptEvaluator {

	protected NashornScriptEngine engine;

	public NashornScriptEvaluator(String name, String function, String script, String file) {
		super(name, function, script, file);

		NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
		engine = (NashornScriptEngine) factory.getScriptEngine("-scripting");

		try {
			engine.eval(this.getScript());
		} catch (ScriptException e) {
			log.error("Unable to evaluate script {} due to error", getName(), e);
			throw new IllegalArgumentException("Unable to evaluate script " + getName(), e);
		}
	}

	@Override
	public Object invoke(Object... args) throws Exception {
		return engine.invokeFunction(getFunction(), args);
	}
}