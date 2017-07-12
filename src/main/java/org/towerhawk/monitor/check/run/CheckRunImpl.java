package org.towerhawk.monitor.check.run;

import lombok.Getter;
import org.towerhawk.monitor.check.Check;

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

	protected CheckRunImpl(Status status, Throwable error, String message,
												 Map<String, Object> context, Duration duration,
												 ZonedDateTime startTime, ZonedDateTime endTime,
												 ZonedDateTime failingSince, boolean timedOut,
												 Check check, CheckRun previousCheckRun) {
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
}
