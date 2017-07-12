package org.towerhawk.monitor.check.threshold.eval;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.MathContext;

@Getter
@Accessors(chain = true)
public class NumericThresholdEvaluator {

	/**
	 * alert if the value is between lower and upper when true
	 */
	private boolean between = false;
	private int precision = 0;
	private double lower = -Double.MAX_VALUE;
	private double upper = Double.MAX_VALUE;

	@Getter(AccessLevel.NONE) private String lowerDigits;
	@Getter(AccessLevel.NONE) private String upperDigits;

	public NumericThresholdEvaluator() {

	}

	public NumericThresholdEvaluator(double lower, double upper) {
		this(lower, upper, false, 0);
	}

	public NumericThresholdEvaluator(double lower, double upper, boolean between, int precision) {
		this.lower = lower;
		this.upper = upper;
		this.between = between;
		this.precision = precision;
		lowerDigits = round(lower);
		upperDigits = round(upper);
	}

	public NumericThresholdEvaluator setLower(double lower) {
		this.lower = lower;
		lowerDigits = round(lower);
		return this;
	}

	public NumericThresholdEvaluator setUpper(double upper) {
		this.upper = upper;
		upperDigits = round(upper);
		return this;
	}

	public NumericThresholdEvaluator setBetween(boolean between) {
		this.between = between;
		return this;
	}

	public NumericThresholdEvaluator setPrecision(int precision) {
		this.precision = precision;
		return this;
	}

	public boolean evaluate(double value) {
		if (between) {
			return value >= lower && value <= upper;
		} else {
			return value < lower || value > upper;
		}
	}

	public String evaluateReason(double value) {
		if (between) {
			return String.format("%s is between %s and %s", round(value), lowerDigits, upperDigits);
		} else {
			return String.format("%s is < %s or > %s", round(value), lowerDigits, upperDigits);
		}
	}

	private String round(double value) {
		if (value == Double.MAX_VALUE) {
			return "infinity";
		} else if (value == -Double.MAX_VALUE) {
			return "-infinity";
		}

		BigDecimal bd = new BigDecimal(value);
		return bd.round(new MathContext(precision)).toPlainString();
	}
}