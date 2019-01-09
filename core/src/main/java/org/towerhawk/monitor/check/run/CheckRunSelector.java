package org.towerhawk.monitor.check.run;

import lombok.Getter;
import org.pf4j.Extension;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.descriptors.Prioritizable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

@Getter
@Extension
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
		id,
		type,
		tags,
		priority,
		check,
		previousCheckRun,
		recursiveCheckRun,
		results,
		_all
	}

	protected Status status = null;
	protected Throwable error = null;
	protected String message = null;
	protected Map<String, Object> context = null;
	protected Duration duration = null;
	protected ZonedDateTime startTime = null;
	protected ZonedDateTime endTime = null;
	protected ZonedDateTime failingSince = null;
	protected Boolean timedOut = null;
	protected String id = null;
	protected String type = null;
	protected Set<String> tags = null;
	protected Byte priority = null;
	protected Check check = null;
	protected CheckRun previousCheckRun = null;
	protected Map<String, Object> results = null;

	public CheckRunSelector(CheckRun checkRun, Collection<Field> fields) {
		final Set<Field> fieldSet = new HashSet<>(fields);
		if (fieldSet.contains(Field._all)) {
			for (Field field : Field.values()) {
				if (field != Field.check && field != Field.error) {
					fieldSet.add(field);
				}
			}
		}
		boolean includeRecursiveCheckRun = fieldSet.contains(Field.recursiveCheckRun);
		if (fieldSet.contains(Field.status)) {
			status = checkRun.getStatus();
		}
		if (fieldSet.contains(Field.error)) {
			error = checkRun.getError();
		}
		if (fieldSet.contains(Field.message)) {
			message = checkRun.getMessage();
		}
		context = filterMapForCheckRuns(checkRun.getContext(), Field.context, includeRecursiveCheckRun, fieldSet);
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
		if (fieldSet.contains(Field.id)) {
			id = checkRun.getCheck().getId();
		}
		if (fieldSet.contains(Field.type)) {
			type = checkRun.getCheck().getType();
		}
		if (fieldSet.contains(Field.tags)) {
			tags = checkRun.getCheck().getTags();
		}
		if (fieldSet.contains(Field.type) && checkRun.getCheck() instanceof Prioritizable) {
			priority = ((Prioritizable)checkRun.getCheck()).getPriority();
		}
		if (fieldSet.contains(Field.check)) {
			check = checkRun.getCheck();
		}
		if (fields.contains(Field.previousCheckRun)) {
			previousCheckRun = getPreviousCheckRun();
			if (previousCheckRun != null) {
				Set<Field> newSet = new HashSet<>(fieldSet);
				newSet.remove(Field.previousCheckRun);
				previousCheckRun = new CheckRunSelector(previousCheckRun, newSet);
			}
		}
		results = filterMapForCheckRuns(checkRun.getResults(), Field.results, includeRecursiveCheckRun, fieldSet);
	}

	private Map<String, Object> filterMapForCheckRuns(Map<String, Object> map, Field field, boolean includeRecursiveCheckRun, Set<Field> fieldSet) {
		boolean include = fieldSet.contains(field);
		if ((include || includeRecursiveCheckRun) && map != null) {
			Map<String, Object> filteredMap = new LinkedHashMap<>();
			//This keeps contexts in their same order and should be safe for non-concurrent maps like LinkedHashMap
			map.forEach((k, v) -> {
				if (include || (v instanceof CheckRun)) {
					Object value = v instanceof CheckRun ? new CheckRunSelector((CheckRun) v, fieldSet) : v;
					filteredMap.put(k, value);
				}
			});
			return filteredMap.isEmpty() ? null : filteredMap;
		}
		return null;
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
