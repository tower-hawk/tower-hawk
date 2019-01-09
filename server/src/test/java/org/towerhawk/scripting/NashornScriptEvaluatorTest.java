package org.towerhawk.scripting;

import org.junit.Test;

import static org.junit.Assert.*;

public class NashornScriptEvaluatorTest {
	@Test
	public void invoke() throws Exception {
		NashornScriptEvaluator evaluator = new NashornScriptEvaluator("testInvoke", "fun1", "var fun1 = function(x) {return x + 'xyz'}", null);
		String arg = "This equals abc 123";
		String result = (String)evaluator.invoke(arg);
		assertEquals(arg + "xyz", result);
	}
}