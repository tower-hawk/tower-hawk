package org.towerhawk.monitor.check.run;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.towerhawk.monitor.check.Check;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

public class CheckRunImpl implements CheckRun {

	final private Status status;
	@JsonIgnore
	final private Throwable error;
	final private String message;
	final private Map<String, Object> context;
	final private Duration duration;
	final private ZonedDateTime startTime;
	final private ZonedDateTime endTime;
	final private ZonedDateTime failingSince;
	final private boolean timedOut;
	@JsonIgnore
	private Check check;
	@JsonIgnore
	private CheckRun previousCheckRun;

	protected CheckRunImpl(Status status, Throwable error, String message,
												 Map<String, Object> context, Duration duration, ZonedDateTime startTime, ZonedDateTime endTime,
												 ZonedDateTime failingSince, boolean timedOut, Check check, CheckRun previousCheckRun) {
		this.status = status;
		this.error = error;
		this.message = getMessage(message, error);
		this.context = context;
		this.duration = duration;
		this.startTime = startTime;
		this.endTime = endTime;
		this.failingSince = failingSince;
		this.timedOut = timedOut;
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
	public String getMessage() {
		return message;
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
	public ZonedDateTime getFailingSince() {
		return failingSince;
	}

	@Override
	public boolean isTimedOut() {
		return timedOut;
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
	public void cleanUp() {
		previousCheckRun = null;
		check = null;
	}

	private String getMessage(String message, Throwable error) {
		if (message != null && !message.isEmpty()) {
			return message;
		}
		if (error != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(error.getMessage());
			int count = 0;
			while (error.getCause() != null && ++count <= 3) {
				error = error.getCause();
				sb.append(error.getMessage());
			}
			return sb.toString();
		}
		return null;
	}
}
