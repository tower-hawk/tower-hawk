package org.towerhawk.monitor.check.run.context;

import org.towerhawk.monitor.check.Check;

import java.util.Map;

/**
 * CompletionManager is an interface that is used to allow checks to express
 * dependencies on one another.
 */
public interface CompletionManager {

	void registerChecks(String appId, Map<String, Check> checks);
}
