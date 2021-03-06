package org.towerhawk.monitor.check.evaluation.threshold.constant;

import org.pf4j.Extension;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.evaluation.threshold.Threshold;
import org.towerhawk.serde.resolver.TowerhawkType;

@Extension
@TowerhawkType("success")
public class SuccessThreshold implements Threshold {

	@Override
	public void evaluate(CheckRun.Builder builder, String key, Object value, boolean setMessage, boolean addContext) throws Exception {
		builder.succeeded();
		if (addContext) {
			builder.addContext("reason","always succeeds");
		}
		if (setMessage) {
			builder.message("Always succeeds");
		}
	}
}
