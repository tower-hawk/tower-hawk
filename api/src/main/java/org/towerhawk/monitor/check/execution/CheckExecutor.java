package org.towerhawk.monitor.check.execution;

import org.pf4j.ExtensionPoint;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;

public interface CheckExecutor extends AutoCloseable, ExtensionPoint {

	/**
	 * Similar to a @PostConstruct. This method should be called by the deserialization
	 * framework to allow checks to initialize any state they need to, like caching a
	 * computation, setting default objects, or starting a background thread.
	 *
	 * @param checkExecutor The previous {@link CheckExecutor} that was defined with the same app and Id
	 * @param check         The {@link Check} that is running this CheckExecutor which should already be initialized.
	 * @param config        The Config provided so CheckExecutors can get defaults and dynamically configure themselves.
	 * @throws Exception The calling framework should handle any exceptions this throws,
	 *                   so there is little need to handle exceptions inside the method
	 */
	void init(CheckExecutor checkExecutor, Check check, Config config) throws Exception;

	/**
	 * This is the method each concrete class should implement
	 * The builder is retained by the holding ${@link Check}
	 * class and used to set values like getDuration,
	 * getStartTime, getEndTime, and other things that should
	 * be handled in a standard way.
	 *
	 * @param builder
	 */
	ExecutionResult execute(CheckRun.Builder builder, RunContext context) throws Exception;

}
