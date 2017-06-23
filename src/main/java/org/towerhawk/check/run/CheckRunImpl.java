package org.towerhawk.check.run;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.towerhawk.check.Check;

import java.util.Map;

public class CheckRunImpl implements CheckRun {

	@JsonSerialize
	final private Status status;
	@JsonSerialize
	final private boolean unknown;
	@JsonIgnore
	final private Throwable error;
	@JsonSerialize
	final private String errorMessage;
	@JsonSerialize
	final private Map<String, Object> context;
	@JsonSerialize
	final private long runTimeNanos;
	@JsonSerialize
	final private long startTimeMillis;
	@JsonSerialize
	final private long endTimeMillis;
	@JsonSerialize
	final private long consecutiveFailures;
	@JsonSerialize
	final private boolean timedOut;
	@JsonSerialize
	final private long retries;
	@JsonIgnore
	final private Check check;
	@JsonIgnore
	final private CheckRun previousCheckRun;

	protected CheckRunImpl(Status status, boolean unknown, Throwable error,
												 Map<String, Object> context, long runTimeNanos, long startTimeMillis, long endTimeMillis,
												 long consecutiveFailures, boolean timedOut, long retries, Check check, CheckRun previousCheckRun) {
		this.status = status;
		this.unknown = unknown;
		this.error = error;
		this.errorMessage = error == null ? null : error.getMessage();
		this.context = context;
		this.runTimeNanos = runTimeNanos;
		this.startTimeMillis = startTimeMillis;
		this.endTimeMillis = endTimeMillis;
		this.consecutiveFailures = consecutiveFailures;
		this.timedOut = timedOut;
		this.retries = retries;
		this.check = check;
		this.previousCheckRun = previousCheckRun;
	}

	@Override
	public Status status() {
		return status;
	}

	@Override
	public boolean unknown() {
		return unknown;
	}

	@Override
	public Throwable getError() {
		return error;
	}

	@Override
	public String errorMessage() {
		return errorMessage;
	}

	@Override
	public Map<String, Object> context() {
		return context;
	}

	@Override
	public long runTimeNanos() {
		return runTimeNanos;
	}

	@Override
	public long startTimeMillis() {
		return startTimeMillis;
	}

	@Override
	public long endTimeMillis() {
		return endTimeMillis;
	}

	@Override
	public long consecutiveFailures() {
		return consecutiveFailures;
	}

	@Override
	public boolean timedOut() {
		return timedOut;
	}

	@Override
	public long retries() {
		return retries;
	}

	@Override
	public Check check() {
		return check;
	}

	@Override
	public CheckRun previousCheckRun() {
		return previousCheckRun;
	}

	@Override
	public int compareTo(CheckRun o) {
		//sort by status ordinal
		int ordinal = Integer.compare(status.getOrdinal(), o.status().getOrdinal());
		//return -ordinal to sort by status desc and if equal then by priority
		int returnVal = ordinal != 0 ? -ordinal : Integer.compare(check.getPriority(), o.check().getPriority());
		return returnVal;
	}
}
