package org.towerhawk.monitor.check.evaluation.threshold;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.towerhawk.monitor.check.evaluation.threshold.builder.SimpleNumericBuilder;
import org.towerhawk.monitor.check.evaluation.threshold.eval.NumericThresholdEvaluator;
import org.towerhawk.monitor.check.evaluation.transform.numeric.DoubleTransform;
import org.towerhawk.monitor.check.run.CheckRun;

@Getter
@Setter
public class SimpleNumericThreshold implements Threshold {

	private NumericThresholdEvaluator warningThreshold;
	private NumericThresholdEvaluator criticalThreshold;
	private DoubleTransform doubleTransform = new DoubleTransform();

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
				new NumericThresholdEvaluator(critLower, critUpper, between, 0));
	}

	public SimpleNumericThreshold(
			double warnLower,
			double warnUpper,
			double critLower,
			double critUpper,
			boolean between,
			int precision
	) {
		this(new NumericThresholdEvaluator(warnLower, warnUpper, between, precision),
				new NumericThresholdEvaluator(critLower, critUpper, between, precision));
	}

	@JsonCreator
	public SimpleNumericThreshold(
			@JsonProperty("warning") NumericThresholdEvaluator warningThreshold,
			@JsonProperty("critical") NumericThresholdEvaluator criticalThreshold
	) {
		this.warningThreshold = warningThreshold;
		this.criticalThreshold = criticalThreshold;
	}

	@Override
	public void evaluate(CheckRun.Builder builder, String key, Object val, boolean setMessage, boolean addContext) throws Exception {
		try {
			double value = (double) doubleTransform.transform(val);
			if (criticalThreshold.evaluate(value)) {
				builder.critical();
				if (addContext) {
					builder.addContext("criticalThreshold", criticalThreshold.evaluateReason(value));
				}
				if (setMessage) {
					builder.message(criticalThreshold.evaluateReason(value));
				}
			} else if (warningThreshold.evaluate(value)) {
				builder.warning();
				if (addContext) {
					builder.addContext("warningThreshold", warningThreshold.evaluateReason(value));
				}
				if (setMessage) {
					builder.message(warningThreshold.evaluateReason(value));
				}
			} else {
				builder.succeeded();
			}
		} catch (Exception e) {
			builder.critical().error(new IllegalArgumentException("Cannot coerce value '" + val.toString() + "' of type " + val.getClass() + " to Number", e));
		}
	}

	public static SimpleNumericBuilder builder() {
		return new SimpleNumericBuilder();
	}
}
