package org.towerhawk.monitor.check.threshold;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.threshold.builder.SimpleNumericBuilder;
import org.towerhawk.monitor.check.threshold.eval.NumericThresholdEvaluator;
import org.towerhawk.serde.resolver.ThresholdType;

@Getter
@Setter
@Builder
@ThresholdType("numeric")
public class SimpleNumericThreshold implements Threshold {

	private NumericThresholdEvaluator warningThreshold;
	private NumericThresholdEvaluator criticalThreshold;
	private boolean addContext = true;
	private boolean setMessage = true;

	public SimpleNumericThreshold(
		NumericThresholdEvaluator warningThreshold,
		NumericThresholdEvaluator criticalThreshold
	) {
		this(warningThreshold, criticalThreshold, false, false);
	}

	public SimpleNumericThreshold(
		double warnLower,
		double warnUpper,
		double critLower,
		double critUpper
	) {
		this(warnLower, warnUpper, critLower, critUpper, false);
	}

	public SimpleNumericThreshold(
		double warnLower,
		double warnUpper,
		double critLower,
		double critUpper,
		boolean between
	) {
		this(new NumericThresholdEvaluator(warnLower, warnUpper, between, 0),
			new NumericThresholdEvaluator(critLower, critUpper, between, 0),
			true, true);
	}

	public SimpleNumericThreshold(
		double warnLower,
		double warnUpper,
		double critLower,
		double critUpper,
		boolean between,
		int precision,
		boolean addContext,
		boolean setMessage
	) {
		this(new NumericThresholdEvaluator(warnLower, warnUpper, between, precision),
			new NumericThresholdEvaluator(critLower, critUpper, between, precision),
			addContext, setMessage);
	}

	@JsonCreator
	public SimpleNumericThreshold(
		@JsonProperty("warning") NumericThresholdEvaluator warningThreshold,
		@JsonProperty("critical") NumericThresholdEvaluator criticalThreshold,
		@JsonProperty("addContext") boolean addContext,
		@JsonProperty("setMessage") boolean setMessage
	) {
		this.warningThreshold = warningThreshold;
		this.criticalThreshold = criticalThreshold;
		this.addContext = addContext;
		this.setMessage = setMessage;
	}

	@Override
	public CheckRun.Status evaluate(CheckRun.Builder builder, double value) {
		if (criticalThreshold.evaluate(value)) {
			builder.critical();
			if (isAddContext()) {
				builder.addContext("criticalThreshold", criticalThreshold.evaluateReason(value));
			}
			if (isSetMessage()) {
				builder.message(criticalThreshold.evaluateReason(value));
			}
			return CheckRun.Status.CRITICAL;
		} else if (warningThreshold.evaluate(value)) {
			builder.warning();
			if (isAddContext()) {
				builder.addContext("warningThreshold", warningThreshold.evaluateReason(value));
			}
			if (isSetMessage()) {
				builder.message(warningThreshold.evaluateReason(value));
			}
			return CheckRun.Status.WARNING;
		}
		builder.succeeded();
		return CheckRun.Status.SUCCEEDED;
	}

	@Override
	public CheckRun.Status evaluate(CheckRun.Builder builder, String value) {
		try {
			double val = Double.valueOf(value);
			return evaluate(builder, val);
		} catch (Exception e) {
			builder.critical().error(new IllegalArgumentException("Cannot coerce value of type " + value.getClass() + " to Number", e));
			return CheckRun.Status.CRITICAL;
		}
	}

	@Override
	public CheckRun.Status evaluate(CheckRun.Builder builder, Object value) {
		try {
			if (value instanceof Number) {
				double val = ((Number) value).doubleValue();
				return evaluate(builder, val);
			} else {
				return evaluate(builder, value.toString());
			}
		} catch (Exception e) {
			builder.critical().error(new IllegalArgumentException("Cannot coerce value of type " + value.getClass() + " to Number", e));
		}
		return CheckRun.Status.CRITICAL;
	}

	public static SimpleNumericBuilder builder() {
		return new SimpleNumericBuilder();
	}
}
