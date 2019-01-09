package org.towerhawk.monitor.check.execution;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExecutionResult {

	public static final String RESULT = "result";
	public static final String DURATION = "duration";
	private transient long startTime = -1;

	private Map<String, Object> resultMap = new LinkedHashMap<>();

	public ExecutionResult() {

	}

	public ExecutionResult(Map<String, Object> results) {
		resultMap.putAll(results);
	}

	public Map<String, Object> getResults() {
		return Collections.unmodifiableMap(resultMap);
	}

	public Object get(String key) {
		return resultMap.get(key);
	}

	public Object addResult(String key, Object value) {
		return resultMap.put(key, value);
	}

	public Object setResult(Object value) {
		return resultMap.put(RESULT, value);
	}

	public ExecutionResult startTimer(long nanoTime) {
		startTime = nanoTime;
		return this;
	}

	public static ExecutionResult startTimer() {
		return new ExecutionResult().startTimer(System.nanoTime());
	}

	public long complete() {
		return complete(null);
	}

	public long complete(Object result) {
		if (startTime < 0) {
			throw new IllegalStateException("Cannot complete without first starting the timer!");
		}
		long durationNanos = System.nanoTime() - startTime;
		addResult(DURATION, Duration.ofNanos(durationNanos));
		if (result != null) {
			setResult(result);
		}
		startTime = -1;
		return durationNanos;
	}

	public static ExecutionResult of(Object value) {
		return of(RESULT, value);
	}

	public static ExecutionResult of(String key, Object value) {
		ExecutionResult result = new ExecutionResult();
		result.addResult(key, value);
		return result;
	}

	/**
	 * Allows for the creation of a ExecutionResult with varArgs
	 * An array with odd length is considered to have the first result
	 * be have the ${@link #RESULT} key
	 * An array with an even length alternates keys and values
	 *
	 * @param values
	 * @return an instance of a ExecutionResult having corresponding keys and values
	 */
	public static ExecutionResult of(Object... values) {
		ExecutionResult executionResult = new ExecutionResult();
		int i = 1;
		if (values.length > 0 && values.length % 2 == 1) {
			i = 2;
			executionResult.addResult(RESULT, values[0]);
		}
		for (; i < values.length; i += 2) {
			executionResult.addResult(values[i - 1].toString(), values[i]);
		}
		return executionResult;
	}
}
