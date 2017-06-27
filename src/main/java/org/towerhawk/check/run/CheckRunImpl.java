package org.towerhawk.check.run;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.towerhawk.check.Check;
import org.towerhawk.jackson.serializer.DurationSerializer;
import org.towerhawk.jackson.serializer.TemporalAccessorSerializer;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

public class CheckRunImpl implements CheckRun {

	@JsonSerialize
	final private Status status;
	@JsonIgnore
	final private Throwable error;
	@JsonSerialize
	final private String statusMessage;
	@JsonSerialize
	final private Map<String, Object> context;
	@JsonSerialize(using = DurationSerializer.class)
	final private Duration duration;
	@JsonSerialize(using = TemporalAccessorSerializer.class)
	final private ZonedDateTime startTime;
	@JsonSerialize(using = TemporalAccessorSerializer.class)
	final private ZonedDateTime endTime;
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

	protected CheckRunImpl(CheckRun c) {
		this(c.getStatus(), c.getError(), c.getStatusMessage(), c.getContext(),
			c.getDuration(), c.getStartTime(), c.getEndTime(), c.getConsecutiveFailures(),
			c.isTimedOut(), c.getRetries(), c.getCheck(), c.getPreviousCheckRun());
	}

	protected CheckRunImpl(Status status, Throwable error, String statusMessage,
												 Map<String, Object> context, Duration duration, ZonedDateTime startTime, ZonedDateTime endTime,
												 long consecutiveFailures, boolean timedOut, long retries, Check check, CheckRun previousCheckRun) {
		this.status = status;
		this.error = error;
		this.statusMessage = statusMessage != null ? statusMessage : error != null ? error.getMessage() : null;
		this.context = context;
		this.duration = duration;
		this.startTime = startTime;
		this.endTime = endTime;
		this.consecutiveFailures = consecutiveFailures;
		this.timedOut = timedOut;
		this.retries = retries;
		this.check = check;
		this.previousCheckRun = previousCheckRun;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public Throwable getError() {
		return error;
	}

	@Override
	public String getStatusMessage() {
		return statusMessage;
	}

	@Override
	public Map<String, Object> getContext() {
		return context;
	}

	@Override
	public Duration getDuration() {
		return duration;
	}

	@Override
	public ZonedDateTime getStartTime() {
		return startTime;
	}

	@Override
	public ZonedDateTime getEndTime() {
		return endTime;
	}

	@Override
	public long getConsecutiveFailures() {
		return consecutiveFailures;
	}

	@Override
	public boolean isTimedOut() {
		return timedOut;
	}

	@Override
	public long getRetries() {
		return retries;
	}

	@Override
	public Check getCheck() {
		return check;
	}

	@Override
	public CheckRun getPreviousCheckRun() {
		return previousCheckRun;
	}

	@Override
	public int compareTo(CheckRun o) {
		//sort by getStatus ordinal
		int ordinal = Integer.compare(status.getOrdinal(), o.getStatus().getOrdinal());
		//return -ordinal to sort by getStatus desc and if equal then by priority
		int returnVal = ordinal != 0 ? ordinal : Integer.compare(check.getPriority(), o.getCheck().getPriority());
		return returnVal;
	}
}
