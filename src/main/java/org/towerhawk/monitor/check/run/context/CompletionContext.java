package org.towerhawk.monitor.check.run.context;

import org.towerhawk.monitor.check.Check;

public interface CompletionContext {
	void registerCompletion(String checkId, Check check);
}
