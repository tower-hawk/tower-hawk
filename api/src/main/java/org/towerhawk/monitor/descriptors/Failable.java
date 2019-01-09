package org.towerhawk.monitor.descriptors;

import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.Status;

import java.time.Duration;
import java.time.ZonedDateTime;

public interface Failable {

	/**
	 * If the most recent CheckRun is in a failed state, this should tell when this check
	 * entered a failed state. It should keep a consistent time until the check successfully
	 * completes.
	 *
	 * @return The first time this check started failing recently.
	 */
	ZonedDateTime getFailingSince();

	/**
	 * The amount of time this check needs to fail before it will actually be reported.
	 *
	 * @return A duration representing how long this check needs to be failing for
	 * before it is reported. If the duration has not passed, the ${@link CheckRun} will
	 * be set to ${@link Status#SUCCEEDED}
	 */
	Duration getAllowedFailureDuration();
}
