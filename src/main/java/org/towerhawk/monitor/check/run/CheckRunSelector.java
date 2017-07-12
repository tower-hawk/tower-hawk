package org.towerhawk.monitor.check.run;

import lombok.Getter;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.spring.config.Configuration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CheckRunSelector implements CheckRun {

	public enum Field {
		status,
		error,
		message,
		context,
		duration,
		startTime,
		endTime,
		failingSince,
		timedOut,
		check,
		previousCheckRun,
		recursiveCheckRun
	}

	private Status status = null;
	private Throwable error = null;
	private String message = null;
	private Map<String, Object> context = null;
	private Duration duration = null;
	private ZonedDateTime startTime = null;
	private ZonedDateTime endTime = null;
	private ZonedDateTime failingSince = null;
	private Boolean timedOut = null;
	private Check check = null;
	private CheckRun previousCheckRun = null;

	public CheckRunSelector(CheckRun checkRun, Collection<Field> fields, Configuration configuration) {
		if (fields == null) {
			fields = configuration.getCheckRunDefaultFields();
		}
		final Set<Field> fieldSet = new HashSet<>(fields);
		if (fieldSet.contains(Field.status)) {
			status = checkRun.getStatus();
		}
		if (fieldSet.contains(Field.error)) {
			error = checkRun.getError();
		}
		if (fieldSet.contains(Field.message)) {
			message = checkRun.getMessage();
		}
		boolean includeContext = fieldSet.contains(Field.context);
		boolean includeRecursiveCheckRun = fieldSet.contains(Field.recursiveCheckRun);
		if (includeContext || includeRecursiveCheckRun) {
			if (checkRun.getContext() != null) {
				context = new LinkedHashMap<>();
				//This keeps contexts in their same order and should be safe for non-concurrent maps like LinkedHashMap
				checkRun.getContext().entrySet().stream().forEachOrdered(e -> {
					if (includeContext || e.getValue() instanceof CheckRun) {
						Object value = e.getValue() instanceof CheckRun ? new CheckRunSelector((CheckRun) e.getValue(), fieldSet, configuration) : e.getValue();
						context.put(e.getKey(), value);
					}
				});
				if (context.isEmpty()) {
					context = null;
				}
			}
		}
		if (fieldSet.contains(Field.duration)) {
			duration = checkRun.getDuration();
		}
		if (fieldSet.contains(Field.startTime)) {
			startTime = checkRun.getStartTime();
		}
		if (fieldSet.contains(Field.endTime)) {
			endTime = checkRun.getEndTime();
		}
		if (fieldSet.contains(Field.failingSince)) {
			failingSince = checkRun.getFailingSince();
		}
		if (fieldSet.contains(Field.timedOut)) {
			timedOut = checkRun.isTimedOut();
		}
		if (fieldSet.contains(Field.check)) {
			check = checkRun.getCheck();
		}
		if (fields.contains(Field.previousCheckRun)) {
			previousCheckRun = checkRun.getPreviousCheckRun();
		}
	}

	@Override
	public boolean isTimedOut() {
		return timedOut == null ? false : timedOut;
	}

	@Override
	public void cleanUp() {
		check = null;
		previousCheckRun = null;
		context = null;
	}
}
