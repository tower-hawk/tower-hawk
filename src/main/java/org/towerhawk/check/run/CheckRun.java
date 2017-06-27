package org.towerhawk.check.run;

import org.towerhawk.check.Check;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public interface CheckRun extends Comparable<CheckRun> {

	enum Status {
		CRITICAL(0),
		WARNING(1),
		UNKNOWN(2),
		SUCCEEDED(3);

		int ordinal;

		Status(int ordinal) {
			this.ordinal = ordinal;
		}

		int getOrdinal() {
			return ordinal;
		}
	}

	Status getStatus();

	Throwable getError();

	String getStatusMessage();

	Map<String, Object> getContext();

	Duration getDuration();

	ZonedDateTime getStartTime();

	ZonedDateTime getEndTime();

	long getConsecutiveFailures();

	long getRetries();

	boolean isTimedOut();

	Check getCheck();

	CheckRun getPreviousCheckRun();

	static Builder builder(Check check) {
		return new Builder(check, check.getLastCheckRun());
	}

	class Builder {

		private Status status;
		private boolean unknownIsCritical = true;
		private Throwable error = null;
		private String statusMessage = null;
		private Map<String, Object> context = new LinkedHashMap<>();
		private Duration duration = Duration.ZERO;
		private ZonedDateTime startTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
		private ZonedDateTime endTime = startTime;
		private long consecutiveFailures = 0;
		private boolean timedOut = false;
		private long retries = 0;
		private Check check;
		private CheckRun previousCheckRun;
		private CheckRun checkRun;

		Builder(Check check, CheckRun previousCheckRun) {
			this.check = check;
		}

		public Builder succeeded() {
			this.status = Status.SUCCEEDED;
			return this;
		}

		public Builder warning() {
			this.status = Status.WARNING;
			return this;
		}

		public Builder unknown() {
			this.status = Status.UNKNOWN;
			return this;
		}

		public Builder critical() {
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

		public Builder setStatusMessage(String statusMessage) {
			this.statusMessage = statusMessage;
			return this;
		}

		public Builder addContext(String key, Object value) {
			if (value != null) {
				context.put(key, value);
			}
			return this;
		}

		public Builder addContext(Map<String, Object> context) {
			this.context.putAll(context);
			return this;
		}

		public Builder durationNanos(long durationNanos) {
			return duration(Duration.ofNanos(durationNanos));
		}

		public Builder duration(Duration duration) {
			this.duration = duration;
			return this;
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

		public Builder consecutiveFailures(long consecutiveFailures) {
			this.consecutiveFailures = consecutiveFailures;
			return this;
		}

		public Builder timedOut(boolean timedOut) {
			this.timedOut = timedOut;
			return this;
		}

		public Builder retries(long retries) {
			this.retries = retries;
			return this;
		}

		public CheckRun build() {
			if (checkRun == null) {
				if (context.isEmpty()) {
					context = null;
				}
				if (unknownIsCritical && status == Status.UNKNOWN) {
					status = Status.CRITICAL;
				} else if (status == Status.UNKNOWN) {
					status = Status.WARNING;
				}
				checkRun = new CheckRunImpl(status, error, statusMessage, context, duration, startTime, endTime
					, consecutiveFailures, timedOut, retries, check, previousCheckRun);
			}
			return checkRun;
		}
	}

}
