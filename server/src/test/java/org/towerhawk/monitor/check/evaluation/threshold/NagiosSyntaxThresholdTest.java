package org.towerhawk.monitor.check.evaluation.threshold;

import org.junit.Before;
import org.junit.Test;
import org.towerhawk.monitor.check.TestCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.Status;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.towerhawk.monitor.check.run.Status.CRITICAL;
import static org.towerhawk.monitor.check.run.Status.SUCCEEDED;
import static org.towerhawk.monitor.check.run.Status.WARNING;

public class NagiosSyntaxThresholdTest {

	private CheckRun.Builder builder;

	@Before
	public void setup() {
		builder = CheckRun.builder(new TestCheck()).succeeded();
	}

	@Test
	public void evaluateDouble() throws Exception {
		//warn if < 10 critical if > 10
		Map<Integer, Status> expected = new LinkedHashMap<>();
		Threshold threshold = new NagiosSyntaxThreshold("10", "20");
		expected.put(-1, CRITICAL);
		expected.put(5, SUCCEEDED);
		expected.put(15, WARNING);
		expected.put(25, CRITICAL);
		evaluateMultiple(threshold, expected);
		threshold = new NagiosSyntaxThreshold("30:", "25:");
		expected.put(35, SUCCEEDED);
		expected.put(28, WARNING);
		expected.put(23, CRITICAL);
		evaluateMultiple(threshold, expected);
		threshold = new NagiosSyntaxThreshold("~:10", "~:20");
		expected.put(9, SUCCEEDED);
		expected.put(12, WARNING);
		expected.put(22, CRITICAL);
		evaluateMultiple(threshold, expected);
		threshold = new NagiosSyntaxThreshold("10:20", "0:30");
		expected.put(-3, CRITICAL);
		expected.put(3, WARNING);
		expected.put(13, SUCCEEDED);
		expected.put(23, WARNING);
		expected.put(33, CRITICAL);
		evaluateMultiple(threshold, expected);
		threshold = new NagiosSyntaxThreshold("@0:30", "@10:20");
		expected.put(-4, SUCCEEDED);
		expected.put(4, WARNING);
		expected.put(14, CRITICAL);
		expected.put(24, WARNING);
		expected.put(34, SUCCEEDED);
		evaluateMultiple(threshold, expected);
	}

	private void evaluateMultiple(Threshold threshold, Map<Integer, Status> expected) {
		expected.forEach((k, v) -> {
			builder.forceSucceeded();
			try {
				threshold.evaluate(builder, "replaceThisDefaultValue", k.doubleValue(), false, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			assertTrue(String.format("Expected value of %s but got %s for value %d", v, builder.getStatus(), k), v == builder.getStatus());
		});
		expected.clear();
	}

	@Test
	public void evaluateString() throws Exception {
		Threshold threshold = new NagiosSyntaxThreshold("10", "20");
		threshold.evaluate(builder, "replaceThisDefaultValue", "5", false, false);
		assertTrue("Expected that a string as a number succeeds on evaluator", builder.getStatus() == SUCCEEDED);
		threshold.evaluate(builder, "replaceThisDefaultValue", "someString", false, false);
		assertTrue("Expected an error but got none", builder.build().getError() != null);
	}

	@Test
	public void evaluateObject() throws Exception {
		Threshold threshold = new NagiosSyntaxThreshold("10", "20");
		threshold.evaluate(builder, "replaceThisDefaultValue", new Long(5), false, false);
		assertTrue("Expected that a Number type succeeds on evaluator", builder.getStatus() == SUCCEEDED);
		threshold.evaluate(builder, "replaceThisDefaultValue", new Object(), false, false);
		assertTrue(builder.build().getError() != null);
	}

}