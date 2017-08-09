package org.towerhawk.monitor.check.threshold;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.Status;
import org.towerhawk.serde.resolver.ThresholdType;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@ThresholdType("regex")
public class SimpleRegexThreshold implements Threshold {

	private String regex;
	private boolean noMatchIsCritical = true;
	private boolean addContext;
	private boolean setMessage;

	@Override
	public Status evaluate(CheckRun.Builder builder, double value) {
		builder.critical();
		throw new UnsupportedOperationException("Cannot evaluate a number (" + String.valueOf(value) + ") against a string check");
	}

	@Override
	public Status evaluate(CheckRun.Builder builder, String value) {
		boolean matches = value.matches(regex);
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
		return builder.getStatus();
	}

	@Override
	public Status evaluate(CheckRun.Builder builder, Object value) {
		return evaluate(builder, value.toString());
	}
}
