package org.towerhawk.monitor.check.run;

import lombok.Getter;
import org.pf4j.ExtensionPoint;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.execution.ExecutionResult;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CheckRun is meant to be an immutable representation of Check::run()
 */
public interface CheckRun extends ExtensionPoint, Comparable<CheckRun> {

	/**
	 * @return The status of this CheckRun.
	 */
	Status getStatus();

	/**
	 * If any Exceptions are thrown inside of Check::run() they can be retrieved here.
	 *
	 * @return Any errors experienced by the check, null otherwise
	 */
	Throwable getError();

	/**
	 * A message that the check can set to summarize the call to Check::run()
	 *
	 * @return The message that was set by the check
	 */
	String getMessage();

	/**
	 * This allows for any context to be added. This is useful when a CheckRun contains
	 * many other CheckRun instances.
	 *
	 * @return
	 */
	Map<String, Object> getContext();

	/**
	 * @return The duration of the check.
	 */
	Duration getDuration();

	/**
	 * @return The ZonedDateTime that the check started
	 */
	ZonedDateTime getStartTime();

	/**
	 * @return The ZonedDateTime that the check ended
	 */
	ZonedDateTime getEndTime();

	/**
	 * @return The time that the check started failing, if in a failing state
	 */
	ZonedDateTime getFailingSince();

	/**
	 * @return true if the check needed to be interrupted, false otherwise
	 */
	boolean isTimedOut();

	/**
	 * @return The check that is tied to this CheckRun
	 */
	Check getCheck();

	/**
	 * @return The CheckRun that happened before this CheckRun. This can make a linked list.
	 */
	CheckRun getPreviousCheckRun();

	/**
	 * @return The results of the ExecutionResult of this check run.
	 */
	Map<String, Object> getResults();

	/**
	 * This allows a CheckRun to remove some state so that a memory leak is not created by
	 * holding on to all CheckRun instances since jvm startup
	 */
	void cleanUp();

	/**
	 * CheckRuns can be compared to one another. The default implementation compares CheckRuns
	 * by the ordinal value of Status and if they're equal then by the underlying Check
	 * and if those are equal then by getDuration()
	 *
	 * @param c
	 * @return
	 */
	default int compareTo(CheckRun c) {
		//return ordinal to sort by status and if equal
		if (c.getStatus() == null || getStatus() == null) {
			return -1;
		}
		int sort = Integer.compare(getStatus().getOrdinal(), c.getStatus().getOrdinal());
		if (sort == 0) {
			//otherwise sort by how the checks sort
			sort = getCheck().compareTo(c.getCheck());
			if (sort == 0) {
				//If still equal, sort by how long they took with longer durations first
				sort = -getDuration().compareTo(c.getDuration());
			}
		}
		return sort;
	}

	/**
	 * Returns an instance of CheckRun.Builder.
	 *
	 * @param check - sets the underlying check
	 * @return
	 */
	static Builder builder(Check check) {
		return builder(check, check.getLastCheckRun());
	}

	static Builder builder(Check check, BuilderAdapter adapter) {
		return builder(check, check.getLastCheckRun(), adapter);
	}

	static Builder builder(Check check, CheckRun checkRun) {
		return new Builder(check, checkRun, Builder.getDefaultAdapter());
	}

	static Builder builder(Check check, CheckRun checkRun, BuilderAdapter adapter) {
		return new Builder(check, checkRun, adapter);
	}

	/**
	 * Used to build a CheckRun from another CheckRun, especially useful if a check
	 * was active and now is not so that context and message can be kept, but the Status
	 * can be changed.
	 *
	 * @param checkRun
	 * @return
	 */
	static Builder builder(CheckRun checkRun) {
		return new Builder(checkRun, Builder.getDefaultAdapter());
	}

	static Builder builder(CheckRun checkRun, BuilderAdapter adapter) {
		return new Builder(checkRun, adapter);
	}

	@Getter
	class Builder {

		private static BuilderAdapter defaultAdapter;

		public static void setDefaultAdapter(BuilderAdapter adapter) {
			defaultAdapter = adapter;
		}

		public static BuilderAdapter getDefaultAdapter() {
			return defaultAdapter;
		}

		private Status status = Status.SUCCEEDED;
		private boolean unknownIsCritical = true;
		private Throwable error = null;
		private String message = null;
		private Map<String, Object> context = new LinkedHashMap<>();
		private Duration duration = Duration.ZERO;
		private ZonedDateTime startTime = ZonedDateTime.now();
		private ZonedDateTime endTime = startTime;
		private ZonedDateTime failingSince = null;
		private boolean timedOut = false;
		private Check check;
		private CheckRun previousCheckRun;
		private CheckRun checkRun;
		private long startNanos = System.nanoTime();
		private Map<String, Object> results;
		private BuilderAdapter adapter;

		Builder(Check check, CheckRun previousCheckRun, BuilderAdapter adapter) {
			this.check = check;
			this.previousCheckRun = previousCheckRun;
			this.adapter = adapter;
		}

		Builder(CheckRun source, BuilderAdapter adapter) {
			this.status = source.getStatus();
			this.error = source.getError();
			this.message = source.getMessage();
			this.context = source.getContext();
			this.duration = source.getDuration();
			this.startTime = source.getStartTime();
			this.endTime = source.getEndTime();
			this.failingSince = source.getFailingSince();
			this.timedOut = source.isTimedOut();
			this.check = source.getCheck();
			this.previousCheckRun = source;
			this.results = source.getResults();
			this.adapter = adapter;
		}

		public Status getStatus() {
			return status;
		}

		public Builder succeeded() {
			if (this.status == null) {
				this.status = Status.SUCCEEDED;
			}
			return this;
		}

		public Builder forceSucceeded() {
			this.status = Status.SUCCEEDED;
			return this;
		}

		public Builder unknown() {
			if (status == null || status.ordinal() > Status.UNKNOWN.ordinal()) {
				this.status = Status.UNKNOWN;
			}
			return this;
		}

		public Builder forceUnknown() {
			this.status = Status.UNKNOWN;
			return this;
		}

		public Builder warning() {
			if (status == null || status.ordinal() > Status.WARNING.ordinal()) {
				this.status = Status.WARNING;
			}
			return this;
		}

		public Builder forceWarning() {
			this.status = Status.WARNING;
			return this;
		}

		public Builder critical() {
			return forceCritical();
		}

		//here for completeness
		public Builder forceCritical() {
			this.status = Status.CRITICAL;
			return this;
		}

		public Builder unknownIsCritical(boolean unknownIsCritical) {
			this.unknownIsCritical = unknownIsCritical;
			return this;
		}

		public Builder error(Throwable error) {
			this.error = error;
			return this;
		}

		public Builder message(String statusMessage) {
			this.message = statusMessage;
			return this;
		}

		public Builder addContext(String key, Object value) {
			if (value != null) {
				context.put(key, value);
			}
			return this;
		}

		public Builder durationNanos(long durationNanos) {
			return duration(Duration.ofNanos(durationNanos));
		}

		public Builder duration(Duration duration) {
			this.duration = duration;
			return this;
		}

		public long startTime() {
			long startTime = System.currentTimeMillis();
			startTime(startTime);
			this.startNanos = System.nanoTime();
			return startTime;
		}

		public Builder startTime(ZonedDateTime startTime) {
			this.startTime = startTime;
			return this;
		}

		public Builder startTime(long startTime, ZoneId zoneId) {
			return this.startTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(startTime), zoneId));
		}

		public Builder startTime(long startTime) {
			return this.startTime(startTime, ZoneId.systemDefault());
		}

		public long endTime() {
			long endNanos = System.nanoTime();
			long endTime = System.currentTimeMillis();
			endTime(endTime);
			durationNanos(endNanos - startNanos);
			return endTime;
		}

		public Builder endTime(ZonedDateTime endTime) {
			this.endTime = endTime;
			return this;
		}

		public Builder endTime(long endTime, ZoneId zoneId) {
			return this.endTime(ZonedDateTime.ofInstant(Instant.ofEpochMilli(endTime), zoneId));
		}

		public Builder endTime(long endTime) {
			return this.endTime(endTime, ZoneId.systemDefault());
		}

		public Builder failingSince(ZonedDateTime failingSince) {
			this.failingSince = failingSince;
			return this;
		}

		public Builder timedOut(boolean timedOut) {
			this.timedOut = timedOut;
			return this;
		}

		public Builder result(ExecutionResult result) {
			if (result != null) {
				this.results = result.getResults();
			}
			return this;
		}

		public Check getCheck() {
			return check;
		}

		public CheckRun build() {
			if (checkRun == null) {
				checkRun = actuallyBuild();
			}
			return checkRun;
		}

		private CheckRun actuallyBuild() {
			if (context.isEmpty()) {
				context = null;
			}
			if (unknownIsCritical && status == Status.UNKNOWN) {
				status = Status.CRITICAL;
			} else if (status == Status.UNKNOWN) {
				status = Status.WARNING;
			}
			if (message != null && message.isEmpty()) {
				message = null;
			}
			return adapter.build(new Getter());
		}

		public class Getter {
			public Status getStatus() {
				return status;
			}

			public Throwable getError() {
				return error;
			}

			public String getMessage() {
				return message;
			}

			public Map<String, Object> getContext() {
				return context;
			}

			public Duration getDuration() {
				return duration;
			}

			public ZonedDateTime getStartTime() {
				return startTime;
			}

			public ZonedDateTime getEndTime() {
				return endTime;
			}

			public ZonedDateTime getFailingSince() {
				return failingSince;
			}

			public boolean isTimedOut() {
				return timedOut;
			}

			public Check getCheck() {
				return check;
			}

			public CheckRun getPreviousCheckRun() {
				return previousCheckRun;
			}

			public Map<String, Object> getResults() {
				return results;
			}
		}
	}

	interface BuilderAdapter {
		CheckRun build(Builder.Getter builder);
	}
}
