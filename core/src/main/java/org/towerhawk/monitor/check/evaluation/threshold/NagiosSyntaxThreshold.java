package org.towerhawk.monitor.check.evaluation.threshold;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.evaluation.threshold.eval.NumericThresholdEvaluator;

@Slf4j
public class NagiosSyntaxThreshold extends SimpleNumericThreshold {

	public NagiosSyntaxThreshold(String warning, String critical) {
		this(warning, critical, 0);
	}

	@JsonCreator
	public NagiosSyntaxThreshold(@JsonProperty("warning") String warning
			, @JsonProperty("critical") String critical
			, @JsonProperty("precision") int precision) {
		super(adaptToThresholdEvaluator(warning, precision), adaptToThresholdEvaluator(critical, precision));
	}

	/**
	 * As referenced from the nagios documentation at:
	 * https://nagios-plugins.org/doc/guidelines.html#THRESHOLDFORMAT
	 * <p>
	 * "10"	      < 0 or > 10, (outside the range of {0 .. 10})
	 * "10:"	    < 10, (outside {10 .. ∞})
	 * "~:10"	    > 10, (outside the range of {-∞ .. 10})
	 * "10:20"	  < 10 or > 20, (outside the range of {10 .. 20})
	 * "@10:20"   ≥ 10 and ≤ 20, (inside the range of {10 .. 20})
	 */
	static private NumericThresholdEvaluator adaptToThresholdEvaluator(@NonNull String threshold, int precision) {
		boolean between = false;
		double lower;
		double upper;
		if (threshold.contains("@")) {
			between = true;
			threshold = threshold.replace("@", "");
		}
		String[] thresholdSplit = threshold.split(":");
		if (thresholdSplit.length == 2) {
			if (thresholdSplit[0].equals("~")) {
				lower = -Double.MAX_VALUE;
				upper = Double.valueOf(thresholdSplit[1]);
			} else {
				lower = Double.valueOf(thresholdSplit[0]);
				upper = Double.valueOf(thresholdSplit[1]);
			}
		} else if (thresholdSplit.length == 1) {
			if (threshold.contains(":")) {
				upper = Double.MAX_VALUE;
				lower = Double.valueOf(thresholdSplit[0]);
			} else {
				lower = 0;
				upper = Double.valueOf(thresholdSplit[0]);
			}
		} else {
			throw new IllegalArgumentException("Threshold must be in the form of (@|~|\\d):?\\d?");
		}
		if (lower > upper) {
			throw new IllegalArgumentException("The lower bound cannot be greater than the upper bound");
		}
		return new NumericThresholdEvaluator(lower, upper, between, precision);
	}
}
