package org.towerhawk.monitor.check.evaluation.threshold.constant;

import org.pf4j.Extension;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.evaluation.threshold.Threshold;
import org.towerhawk.serde.resolver.TowerhawkType;

@Extension
@TowerhawkType("timeout")
public class TimeOutThreshold implements Threshold {

	@Override
	public void evaluate(CheckRun.Builder builder, String key, Object value, boolean setMessage, boolean addContext) throws Exception {
		Thread.sleep(Long.MAX_VALUE);
	}
}
