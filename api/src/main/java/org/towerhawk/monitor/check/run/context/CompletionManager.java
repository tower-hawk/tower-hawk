package org.towerhawk.monitor.check.run.context;

import org.towerhawk.monitor.check.Check;
import org.towerhawk.serde.resolver.ExtensibleAPI;

import java.util.Map;

/**
 * CompletionManager allows checks to express dependencies on one another.
 */
public interface CompletionManager {

	void registerChecks(String appId, Map<String, Check> checks);
}
