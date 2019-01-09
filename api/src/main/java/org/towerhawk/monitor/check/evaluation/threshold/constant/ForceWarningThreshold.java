package org.towerhawk.monitor.check.evaluation.threshold.constant;

import org.pf4j.Extension;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.evaluation.threshold.Threshold;
import org.towerhawk.serde.resolver.TowerhawkType;

@Extension
@TowerhawkType("forceWarning")
public class ForceWarningThreshold implements Threshold {

	@Override
	public void evaluate(CheckRun.Builder builder, String key, Object value, boolean setMessage, boolean addContext) throws Exception {
		builder.forceWarning();
	}
}
