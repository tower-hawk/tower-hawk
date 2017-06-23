package org.towerhawk.check.run;

import org.towerhawk.check.Check;

import java.util.LinkedHashMap;
import java.util.Map;

public interface CheckRun extends Comparable<CheckRun> {

	enum Status {
		SUCCEEDED(0),
		WARNING(1),
		CRITICAL(2);

		int ordinal;

		Status(int ordinal) {
			this.ordinal = ordinal;
		}

		int getOrdinal() {
			return ordinal;
		}
	}

	Status status();

	boolean unknown();

	Throwable getError();

	String errorMessage();

	Map<String, Object> context();

	long runTimeNanos();

	long startTimeMillis();

	long endTimeMillis();

	long consecutiveFailures();

	long retries();

	boolean timedOut();

	Check check();

	CheckRun previousCheckRun();

	static Builder builder(Check check) {
		return new Builder(check, check.getLastCheckRun());
	}

	class Builder {

		private Status status;
		private boolean unknown = false;
		private boolean unknownIsCritical = true;
		private Throwable error = null;
		private Map<String, Object> context = new LinkedHashMap<>();
		private long runTimeNanos = -1;
		private long startTimeMillis = -1;
		private long endTimeMillis = -1;
		private long consecutiveFailures = 0;
		private boolean timedOut = false;
		private long retries = 0;
		private Check check;
		private CheckRun previousCheckRun;

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

		public Builder critical() {
			this.status = Status.CRITICAL;
			return this;
		}

		public Builder unknown(boolean unknown) {
			this.unknown = unknown;
			return this;
		}

		public Builder unknown() {
			this.unknown = true;
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

		public Builder runTimeNanos(long runTimeNanos) {
			this.runTimeNanos = runTimeNanos;
			return this;
		}

		public Builder startTimeMillis(long startTimeMillis) {
			this.startTimeMillis = startTimeMillis;
			return this;
		}

		public Builder endTimeMillis(long endTimeMillis) {
			this.endTimeMillis = endTimeMillis;
			return this;
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
			if (context.isEmpty()) {
				context = null;
			}
			if (unknown && unknownIsCritical && status != Status.CRITICAL) {
				status = Status.CRITICAL;
			} else if (unknown && status != Status.WARNING) {
				status = Status.WARNING;
			}
			CheckRun checkRun = new CheckRunImpl(status, unknown, error, context, runTimeNanos, startTimeMillis, endTimeMillis
				, consecutiveFailures, timedOut, retries, check, previousCheckRun);
			return checkRun;
		}
	}

}
