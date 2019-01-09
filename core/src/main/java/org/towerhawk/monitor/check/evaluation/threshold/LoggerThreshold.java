package org.towerhawk.monitor.check.evaluation.threshold;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.serde.resolver.TowerhawkType;

@Getter
@Setter
@Slf4j
@Extension
@TowerhawkType("logger")
public class LoggerThreshold implements Threshold {

	@Override
	public void evaluate(CheckRun.Builder builder, String key, Object value, boolean setMessage, boolean addContext) throws Exception {
		log.info("{}: {}", builder.getCheck().getFullName(), value.toString());
		builder.succeeded();
		if (addContext) {
			builder.addContext("value", value.toString());
		}
	}
}
