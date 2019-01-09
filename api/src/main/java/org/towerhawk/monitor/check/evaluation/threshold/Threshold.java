package org.towerhawk.monitor.check.evaluation.threshold;

import org.pf4j.ExtensionPoint;
import org.towerhawk.monitor.check.run.CheckRun;

/**
 * Classes that implement the Threshold interface should mark
 * the builder status as successful, warning, or critical.
 * They should optionally add a context or set a message
 * if the parameters are set to help with trouble shooting
 */
public interface Threshold extends ExtensionPoint {

	void evaluate(CheckRun.Builder builder, String key, Object value, boolean setMessage, boolean addContext) throws Exception;
}
