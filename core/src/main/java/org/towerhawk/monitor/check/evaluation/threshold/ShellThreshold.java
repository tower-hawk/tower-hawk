package org.towerhawk.monitor.check.evaluation.threshold;

import org.towerhawk.monitor.check.evaluation.transform.numeric.IntegerTransform;
import org.towerhawk.monitor.check.run.CheckRun;

public class ShellThreshold implements Threshold {

	private static IntegerTransform integerTransform = new IntegerTransform();

	@Override
	public void evaluate(CheckRun.Builder builder, String key, Object value, boolean setMessage, boolean addContext) throws Exception {
		int val = integerTransform.transform(value);
		switch (val) {
			case 0:
				builder.succeeded();
				break;
			case 1:
				builder.warning();
				break;
			case 2:
				builder.critical();
				break;
			default:
				builder.unknown();
		}
		if (setMessage) {
			builder.message("Value is " + value.toString());
		}
		if (addContext) {
			builder.addContext("value", value);
		}
	}
}
