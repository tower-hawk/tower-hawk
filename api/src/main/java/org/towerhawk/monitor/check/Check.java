package org.towerhawk.monitor.check;

import org.pf4j.ExtensionPoint;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.descriptors.CheckRunnable;
import org.towerhawk.monitor.descriptors.Filterable;

public interface Check extends ExtensionPoint, Comparable<Check>, AutoCloseable, CheckRunnable, Filterable {

	/**
	 * All checks belong to an App.
	 *
	 * @return The App this check belongs to.
	 */
	App getApp();

	/**
	 * Similar to @PostConstruct. This method will be called by the deserialization
	 * framework to allow checks to initialize any state they need to, like caching a
	 * computation, setting default objects, or starting a background thread.
	 *
	 * @param previousCheck The previous check that was defined with the same App and Id
	 * @param app   The app that this check belongs to
	 * @param id    The name of this check used in returning values and in logging
	 */
	void init(Check previousCheck, Config config, App app, String id) throws Exception;
}
