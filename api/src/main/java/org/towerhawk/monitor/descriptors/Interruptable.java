package org.towerhawk.monitor.descriptors;

public interface Interruptable {

	/**
	 * How long to let a check run before interrupting it. If a check is running for
	 * more than getTimeoutMs() milliseconds then it should be cancelled and return a
	 * CheckRun with an Error and the isTimedOut() method returning true.
	 *
	 * @return timeout in milliseconds
	 */
	long getTimeoutMs();

	/**
	 * Returns how many milliseconds this check has to finish running. Useful when using
	 * other libraries that have timeouts so they can timeout before the check does to do
	 * better error handling.
	 *
	 * @param throwException Whether or not to throw an IllegalStateException indicating that
	 *                       this Check has timed out.
	 * @return the number of milliseconds remaining before timeout
	 */
	long getMsRemaining(boolean throwException);

	/**
	 * Equivalent to calling {@code getMsRemaining(true)}
	 * @return the number of milliseconds remaining before timeout
	 */
	default long getMsRemaining() {
		return getMsRemaining(true);
	}
}
