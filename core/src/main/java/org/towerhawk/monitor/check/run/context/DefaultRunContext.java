package org.towerhawk.monitor.check.run.context;

import java.util.HashMap;
import java.util.Map;

public class DefaultRunContext implements RunContext {

	private boolean run = true;
	private boolean saveCheckRun = true;
	private Map<String, Object> context = new HashMap<>(2);
	private CompletionManager completionManager;

	@Override
	public boolean shouldRun() {
		return run;
	}

	public DefaultRunContext setShouldrun(boolean shouldRun) {
		this.run = shouldRun;
		return this;
	}

	@Override
	public boolean saveCheckRun() {
		return saveCheckRun;
	}

	@Override
	public RunContext setSaveCheckRun(boolean saveCheckRun) {
		this.saveCheckRun = saveCheckRun;
		return this;
	}

	@Override
	public CompletionManager getCompletionManager() {
		return completionManager;
	}

	public RunContext setCompletionContext(CompletionManager completionManager) {
		this.completionManager = completionManager;
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
	public RunContext putContext(String key, Object val) {
		context.put(key, val);
		return this;
	}

	@Override
	public RunContext duplicate() {
		DefaultRunContext context = new DefaultRunContext();
		context.completionManager = this.completionManager;
		context.run = this.run;
		context.context.putAll(this.context);
		context.saveCheckRun = this.saveCheckRun;
		return context;
	}
}
