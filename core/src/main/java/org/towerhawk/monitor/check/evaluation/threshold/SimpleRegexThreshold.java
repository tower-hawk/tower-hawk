package org.towerhawk.monitor.check.evaluation.threshold;

import lombok.Getter;
import lombok.Setter;
import org.towerhawk.monitor.check.run.CheckRun;

@Getter
@Setter
public class SimpleRegexThreshold implements Threshold {

	private String regex;
	private boolean noMatchIsCritical = true;

	@Override
	public void evaluate(CheckRun.Builder builder, String key, Object value, boolean setMessage, boolean addContext) throws Exception {
		boolean matches = value.toString().matches(regex);
		if (matches) {
			builder.succeeded();
			if (setMessage) {
				builder.message(value + " matches " + regex);
			}
		} else {
			if (noMatchIsCritical) {
				builder.critical();
			} else {
				builder.warning();
			}
			if (setMessage) {
				builder.message(value + " does not match " + regex);
			}
		}
		if (addContext) {
			builder.addContext("regex", regex)
					.addContext("value", value)
					.addContext("matches", matches);
		}
	}
}
