package org.towerhawk.monitor.descriptors;

import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;

import java.util.List;

public interface CheckRunnable {

	/**
	 * This is where the real work happens. A CheckRun is returned containing information
	 * about how the check went. This can be a synchronized method to ensure that multiple
	 * runs don't happen concurrently. Logic should be in place in this method to see if
	 * a check can run (see canRun()) and if this method gets called concurrently
	 * the second invocation can return the results of the first invocation.
	 *
	 * @param runContext
	 * @return The CheckRun representing the results of this run().
	 */
	CheckRun run(RunContext runContext);

	/**
	 * @return The most recent {@link CheckRun}. Equivalent to getting the last element of
	 * getRecentCheckRuns()
	 */
	CheckRun getLastCheckRun();

	/**
	 * @return A sorted list of check runs with the oldest first and most recent last.
	 */
	List<CheckRun> getRecentCheckRuns();

	/**
	 * Determines if this check is currently running.
	 *
	 * @return true if running, false otherwise
	 */
	boolean isRunning();

	/**
	 * Determines if the check can run right now. The default implementation returns true
	 * if isActive() is true and isRunning() is false and isCached() is false. isCached()
	 * should be called last since that is potentially the most expensive to compute. If this
	 * returns false then CheckRunner implementations can skip calling run() and return
	 * getLastCheckRun()
	 *
	 * @return
	 */
	boolean canRun();
}
