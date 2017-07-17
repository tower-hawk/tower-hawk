package org.towerhawk.monitor.check;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.serde.resolver.CheckTypeResolver;
import org.towerhawk.spring.config.Configuration;

import java.io.Closeable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonTypeIdResolver(CheckTypeResolver.class)
public interface Check extends Comparable<Check>, Closeable {

	long CACHE_MS = -1;
	long TIMEOUT_MS = -1;
	byte PRIORITY = Byte.MIN_VALUE;

	/**
	 * The id for this check. This should match the dictionary key in the configuration yaml.
	 * This must be unique within an App.
	 *
	 * @return the Id of this check
	 */
	String getId();

	default String getFullName() {
		return getApp().getId() + ":" + getId();
	}

	/**
	 * This is where the real work happens. A CheckRun is returned containing information
	 * about how the check went. This can be a synchronized method to ensure that multiple
	 * runs don't happen concurrently. Logic should be in place in this method to see if
	 * a check can run (see canRun()) and if this method gets called concurrently
	 * the second invocation can return the results of the first invocation.
	 *
	 * @param checkContext
	 * @return The CheckRun representing the results of this run().
	 */
	CheckRun run(CheckContext checkContext);

	/**
	 * This determines whether the check is active right now or not. This allows different
	 * strategies to be implemented like daily or weekly schedules. This can also be used
	 * to effectively disable a check.
	 *
	 * @return true if the check can run, false otherwise.
	 */
	default boolean isActive() {
		return true;
	}

	/**
	 * Return how long the check should be cached for. If the last run is less than
	 * getCacheMs() milliseconds ago then the check can return a cached CheckRun.
	 *
	 * @return cache time in milliseconds
	 */
	long getCacheMs();

	/**
	 * How long to let a check run before interrupting it. If a check is running for
	 * more than getTimeoutMs() milliseconds then it should be cancelled and return a
	 * CheckRun with an Error and the isTimedOut() method returning true.
	 *
	 * @return timeout in milliseconds
	 */
	long getTimeoutMs();

	/**
	 * If the most recent CheckRun is in a failed state, this should tell when this check
	 * entered a failed state. It should keep a consistent time until the check successfully
	 * completes.
	 *
	 * @return The first time this check started failing recently.
	 */
	ZonedDateTime getFailingSince();

	/**
	 * Checks with higher priorities must be run first. This method can also be called to
	 * run checks with a certain priority.
	 *
	 * @return the priority of the check
	 */
	byte getPriority();

	/**
	 * An alias for this check. When looking up checks by id, this method should also be
	 * consulted which allows for migration. This should be unique within an App.
	 *
	 * @return
	 */
	String getAlias();

	/**
	 * The type of the check as defined in the yaml configuration. This is available so
	 * that all checks of a type can be run together.
	 *
	 * @return The type defined in the configuration yaml.
	 */
	String getType();

	/**
	 * All checks belong to an App.
	 *
	 * @return The App this check belongs to.
	 */
	App getApp();

	/**
	 * Returns a set of tags that are used to be able to run a subset of checks.
	 *
	 * @return The tags defined in the configuration yaml
	 */
	Set<String> getTags();

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
	 * Determines if this check will return a cached CheckRun instead of actually running.
	 *
	 * @return false if check will actually run, true if a cached CheckRun will be returned
	 */
	boolean isCached();

	/**
	 * Determines if the check can run right now. The default implementation returns true
	 * if isActive() is true and isRunning() is false and isCached() is false. isCached()
	 * should be called last since that is potentially the most expensive to compute. If this
	 * returns false then CheckRunner implementations can skip calling run() and return
	 * getLastCheckRun()
	 *
	 * @return
	 */
	default boolean canRun() {
		return isActive() && !isRunning() && !isCached();
	}

	/**
	 * Similar to a @PostConstruct. This method should be called by the deserialization
	 * framework to allow checks to initialize any state they need to, like caching a
	 * computation, setting default objects, or starting a background thread.
	 *
	 * @param check         The previous check that was defined with the same App and Id
	 * @param configuration The Configuration provided by Spring so that checks can
	 *                      get defaults and dynamically configure themselves.
	 * @param app           The app that this check belongs to
	 * @param id            The name of this check used in returning values and in logging
	 */
	void init(Check check, Configuration configuration, App app, String id);

	/**
	 * Checks can be compared to one another. Checks with a higher getPriority() are
	 * first and when tied, checks with a lower getTimeoutMs() break that tie.
	 *
	 * @param check The check to compare this check to
	 * @return
	 */
	default int compareTo(Check check) {
		// Sort by priority
		int compare = -Integer.compare(getPriority(), check.getPriority());
		if (compare == 0) {
			// Then by timeout so that shortest timeouts get submitted first
			compare = Long.compare(getTimeoutMs(), check.getTimeoutMs());
		}
		return compare;
	}
}
