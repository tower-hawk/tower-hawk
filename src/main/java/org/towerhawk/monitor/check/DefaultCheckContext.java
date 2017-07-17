package org.towerhawk.monitor.check;

import java.util.HashMap;
import java.util.Map;

public class DefaultCheckContext implements CheckContext {

	private boolean run = true;
	private boolean saveCheckRun = true;
	private Map<String, Object> context = new HashMap<>(2);

	@Override
	public boolean shouldRun() {
		return run;
	}

	public DefaultCheckContext setShouldrun(boolean shouldRun) {
		this.run = shouldRun;
		return this;
	}

	@Override
	public boolean saveCheckRun() {
		return saveCheckRun;
	}

	@Override
	public CheckContext setSaveCheckRun(boolean saveCheckRun) {
		this.saveCheckRun = saveCheckRun;
		return this;
	}

	@Override
	public Map<String, Object> getContext() {
		return context;
	}

	@Override
	public Object get(String key) {
		return context.get(key);
	}

	@Override
	public CheckContext putContext(String key, Object val) {
		context.put(key, val);
		return this;
	}

	@Override
	public CheckContext duplicate() {
		return duplicate(this);
	}

	public static DefaultCheckContext duplicate(CheckContext checkContext) {
		DefaultCheckContext duplicate = new DefaultCheckContext();
		duplicate.setShouldrun(checkContext.shouldRun());
		duplicate.setSaveCheckRun(checkContext.saveCheckRun());
		duplicate.context.putAll(checkContext.getContext());
		return duplicate;
	}
}
