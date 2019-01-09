package org.towerhawk.monitor.check.run.impl;

import lombok.Getter;
import lombok.NonNull;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.Status;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

@Getter
public class CheckRunImpl implements CheckRun {

	final private Status status;
	final private Throwable error;
	final private String message;
	private Map<String, Object> context;
	final private Duration duration;
	final private ZonedDateTime startTime;
	final private ZonedDateTime endTime;
	final private ZonedDateTime failingSince;
	final private boolean timedOut;
	private Check check;
	private CheckRun previousCheckRun;
	private Map<String, Object> results;

	protected CheckRunImpl(Status status, Throwable error, String message,
												 Map<String, Object> context, Duration duration,
												 ZonedDateTime startTime, ZonedDateTime endTime,
												 ZonedDateTime failingSince, boolean timedOut,
												 @NonNull Check check, CheckRun previousCheckRun, Map<String, Object> results) {
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
		this.results = results;
	}

	protected CheckRunImpl(Builder.Getter builder) {
		this(builder.getStatus(),
				builder.getError(),
				builder.getMessage(),
				builder.getContext(),
				builder.getDuration(),
				builder.getStartTime(),
				builder.getEndTime(),
				builder.getFailingSince(),
				builder.isTimedOut(),
				builder.getCheck(),
				builder.getPreviousCheckRun(),
				builder.getResults()
		);
	}

	@Override
	public void cleanUp() {
		previousCheckRun = null;
		check = null;
		context = null;
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

	public static class BuilderAdapter implements CheckRun.BuilderAdapter {

		@Override
		public CheckRun build(Builder.Getter builder) {
			return new CheckRunImpl(builder);
		}
	}
}
