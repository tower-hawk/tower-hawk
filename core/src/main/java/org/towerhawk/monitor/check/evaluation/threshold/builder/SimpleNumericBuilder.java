package org.towerhawk.monitor.check.evaluation.threshold.builder;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.towerhawk.monitor.check.evaluation.threshold.SimpleNumericThreshold;
import org.towerhawk.monitor.check.evaluation.threshold.eval.NumericThresholdEvaluator;


@Setter
@Accessors(fluent = true)
public class SimpleNumericBuilder {

	double warnLower = -Double.MAX_VALUE;
	double warnUpper = Double.MAX_VALUE;
	double critLower = -Double.MAX_VALUE;
	double critUpper = Double.MAX_VALUE;
	boolean between = false;
	int precision = 0;
	boolean addContext = false;
	boolean setMessage = false;
	NumericThresholdEvaluator warning = null;
	NumericThresholdEvaluator critical = null;

	public SimpleNumericThreshold build() {
		if (warning != null && critical != null) {
			return new SimpleNumericThreshold(warning, critical);
		} else {
			return new SimpleNumericThreshold(warnLower, warnUpper, critLower, critUpper, between, precision);
		}
	}
}