package org.towerhawk.monitor.check;

import java.util.Map;

public interface CheckContext {

	/**
	 * Whether or not the check should actually run. If false the check can return
	 * a cached CheckRun even if it is outside of the cache window.
	 *
	 * @return
	 */
	boolean shouldRun();

	/**
	 * Whether or not to record this as the last check run. The is useful when an
	 * App won't be running all of its checks, so it doesn't want to cache the results
	 * of that as its last check run.
	 */
	boolean saveCheckRun();

	/**
	 * Some checks should be able to determine whether or not they should be saved
	 * thus overriding what was passed in. This method should ideally be called by
	 * an App or Check during the run() method and should be checked afterward to determine
	 * if the CheckRun produced should be cached.
	 *
	 * @param saveCheckRun
	 * @return
	 */
	CheckContext setSaveCheckRun(boolean saveCheckRun);

	/**
	 * @return A map with any context that a check can use to change its behavior.
	 */
	Map<String, Object> getContext();

	/**
	 * Instead of getting the map, this should return the value of get from the map.
	 *
	 * @param key
	 * @return
	 */
	Object get(String key);

	/**
	 * @param key
	 * @param val
	 * @return
	 */
	CheckContext putContext(String key, Object val);

	/**
	 * Because check contexts can be modified, a new context should be created for each check
	 * This is the method that should be called to get a new object just like the current object
	 *
	 * @return
	 */
	CheckContext duplicate();

}
