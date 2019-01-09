package org.towerhawk.monitor.descriptors;

public interface Cacheable {

	/**
	 * Return how long the check should be cached for. If the last run is less than
	 * getCacheMs() milliseconds ago then the check can return a cached CheckRun.
	 *
	 * @return cache time in milliseconds
	 */
	long getCacheMs();

	/**
	 *
	 * @return true is the check will not run false if it can run
	 */
	default boolean isCached() {
	  return cachedForMs() > 0;
	}

	/**
	 * Determines if this check will return a cached CheckRun instead of actually running.
	 *
	 * @return <= 0 if check will actually run, > 0 if a cached CheckRun will be returned
	 */
	long cachedForMs();
}
