package org.towerhawk.monitor.check;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.active.Active;
import org.towerhawk.monitor.active.Enabled;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.cluster.Cluster;
import org.towerhawk.monitor.check.cluster.NoCluster;
import org.towerhawk.monitor.check.evaluation.Evaluator;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.logging.CheckMDC;
import org.towerhawk.monitor.check.recent.RecentCheckRun;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.Status;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.monitor.descriptors.*;
import org.towerhawk.monitor.schedule.EmptyScheduleCollector;
import org.towerhawk.monitor.schedule.ScheduleCollector;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Getter
@Extension
@TowerhawkType(value = "default", typeField = "type")
public class DefaultCheck implements Check, Activatable, Cacheable, Dependable, Evaluatable, Executable, Failable, Prioritizable, Restartable, Schedulable, Interruptable, Clusterable {

	/**
	 * The id for this check. This should match the dictionary key in the config yaml.
	 * This must be unique within an App.
	 */
	private String id = null;

	/**
	 * The app this Check is tied to.
	 */
	@JsonIgnore
	private App app = null;

	/**
	 * The type of check that this is, or more particularly the type of the
	 * execution that this check holds.
	 */
	@Setter
	private String type = "check";

	/**
	 * An alias for this check. When looking up checks by id, this method should also be
	 * consulted which allows for migration. This should be unique within an App.
	 */
	@Setter
	private String alias = null;

	/**
	 * The fullName of the check - which by default is ${App}:${Check}
	 */
	protected transient String fullName = null;

	/**
	 * The timestamp containing the last time the check finished executing.
	 * This is used for determining cache intervals
	 */
	private transient long runEndTimestamp = 0;

	/**
	 * The timestamp containing the last time the check started executing.
	 */
	private transient long runStartTimestamp = 0;

	/**
	 * The interval used to schedule runs of this check.
	 */
	@Setter
	private ScheduleCollector schedule = new EmptyScheduleCollector();

	@Override
	public ScheduleCollector getScheduleCollector() {
		return schedule;
	}

	/**
	 * This determines whether the check is active right now or not. This allows different
	 * strategies to be implemented like daily or weekly schedules. This can also be used
	 * to effectively disable a check.
	 */
	@Setter
	private Long cacheMs = null;

	/**
	 * How long to let a check run before interrupting it. If a check is running for
	 * more than getTimeoutMs() milliseconds then it should be cancelled and return a
	 * CheckRun with an Error and the isTimedOut() method returning true.
	 */
	@Setter
	private Long timeoutMs = null;

	/**
	 * Checks with higher priorities must be run first. This method can also be called to
	 * run checks with a certain priority.
	 */
	@Setter
	private Byte priority = null;

	/**
	 * The amount of time this check needs to fail before it will actually be reported.
	 *
	 * @return A duration representing how long this check needs to be failing for
	 * before it is reported. If the duration has not passed, the ${@link CheckRun} will
	 * be set to ${@link Status#SUCCEEDED}
	 */
	@Setter
	private Duration allowedFailureDuration = Duration.ZERO;

	/**
	 * If the most recent CheckRun is in a failed state, this should tell when this check
	 * entered a failed state. It should keep a consistent time until the check successfully
	 * completes.
	 */
	private transient ZonedDateTime failingSince = null;

	/**
	 * @return If this check has been set to restarting. If it is restarting, then it will
	 * not return anything other than ${@link Status#SUCCEEDED} until it has actually
	 * completed successfully
	 */
	@Setter
	private transient boolean restarting = false;

	/**
	 * Returns a set of tags that are used to be able to run a subset of checks.
	 */
	private Set<String> tags = new LinkedHashSet<>();

	/**
	 * This determines whether the check is active right now or not. This allows different
	 * strategies to be implemented like daily or weekly schedules. This can also be used
	 * to effectively disable a check.
	 *
	 * @return true if the check can run, false otherwise.
	 */
	@Setter()
	private Active active = new Enabled();

	/**
	 * A representation of CheckRuns so that historical runs can be inspected.
	 */
	@JsonIgnore
	private transient RecentCheckRun recentCheckRuns = new RecentCheckRun();

	/**
	 * Determines if this check is currently running.
	 * true if running, false otherwise
	 */
	@JsonIgnore
	private transient boolean running = false;

	/**
	 * How to deal with a ${@link CheckRun} that is UNKNOWN. By default it is treated
	 * as CRITICAL. Setting this to false will treat UNKOWN as WARNING.
	 */
	@Setter
	private boolean unknownIsCritical = true;

	/**
	 * A reference to the main Configuration so that Executors, Thresholds, and Transforms
	 * can dynamically configure themselves if necessary.
	 */
	@JsonIgnore
	private transient Config config;

	/**
	 * Keeps track of whether this check has been initialized to keep it from being initialized
	 * twice since initialization can be expensive.
	 */
	private transient boolean initialized = false;

	/**
	 * A map of transforms to be executed. Since each transform can have a threshold, it allows
	 * for the results of a single execution to have multiple checks run on it.
	 */
	@Setter
	@NonNull
	private Evaluator evaluator;

	/**
	 * The ${@link CheckExecutor} used to run the checks.
	 */
	@Setter
	@NonNull
	private CheckExecutor executor;

	/**
	 * The {@link Cluster} used to determine if checks can run
	 */
	@Setter
	@NonNull
	private Cluster cluster = new NoCluster();

	@Override
	public final boolean isActive() {
		return initialized && active.isActive();
	}

	@Override
	public byte getPriority() {
		return priority;
	}

	@Override
	public long getTimeoutMs() {
		return timeoutMs;
	}

	@Override
	public long getCacheMs() {
		return cacheMs;
	}

	@Override
	@JsonIgnore
	public CheckRun getLastCheckRun() {
		return recentCheckRuns.getLastRun();
	}

	@JsonIgnore
	@Override
	public List<CheckRun> getRecentCheckRuns() {
		return recentCheckRuns.getRecentCheckRuns();
	}

	@Override
	public long cachedForMs() {
		return getCacheMs() - (System.currentTimeMillis() - runEndTimestamp);
	}

	@Override
	public long getMsRemaining(boolean throwException) {
		long timeRemaining = getTimeoutMs() - (System.currentTimeMillis() - runStartTimestamp);
		if (timeRemaining < 0 && throwException) {
			throw new IllegalStateException("Check is timed out");
		}
		return timeRemaining;
	}

	protected final ZonedDateTime setFailingSince(long epochMillis) {
		return setFailingSince(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()));
	}

	protected final ZonedDateTime setFailingSince() {
		return setFailingSince(ZonedDateTime.now());
	}

	protected final ZonedDateTime setFailingSince(ZonedDateTime failingSince) {
		if (this.failingSince == null) {
			this.failingSince = failingSince;
		}
		return failingSince;
	}

	protected final void clearFailingSince() {
		failingSince = null;
	}

	protected final void setRecentCheckRunSize(int size) {
		recentCheckRuns.setSizeLimit(size);
	}

	private void maybeSuppressFailure(CheckRun.Builder builder, ZonedDateTime failingSince) {
		if (isRestarting()) {
			builder.forceSucceeded().addContext("restarting", true);
			return;
		}
		ZonedDateTime failingTime = failingSince.plus(getAllowedFailureDuration());
		if (failingTime.compareTo(ZonedDateTime.now()) > 0) {
			builder.forceSucceeded().addContext("suppressedFailureUntil", failingTime);
		}
	}

	@Override
	public boolean canRun() {
		return isInitialized() && isActive() && !isRunning() && !isCached() && canExecuteForCluster();
	}

	/**
	 * This is where the real work happens. A ${@link CheckRun} is returned containing information
	 * about how the check went. This can be a synchronized method to ensure that multiple
	 * runs don't happen concurrently. Logic should be in place in this method to see if
	 * a check can run (see canRun()) and if this method gets called concurrently
	 * the second invocation can return the results of the first invocation.
	 *
	 * @param runContext
	 * @return The CheckRun representing the results of this run().
	 */
	@Override
	@Synchronized
	public final CheckRun run(RunContext runContext) {
		CheckMDC.put(this);
		CheckRun checkRun;
		long cachedFor;
		if (!runContext.shouldRun() || !canRun()) {
			if (running) {
				log.debug("Check is already running");
			} else if (!initialized) {
				log.warn("Trying to run check but it is not initialized");
			} else if (!isActive()) {
				log.debug("Check is not active");
				CheckRun lastRun = getLastCheckRun();
				if (lastRun.getStatus() != Status.SUCCEEDED) {
					CheckRun.Builder copyRunBuilder = CheckRun.builder(lastRun);
					copyRunBuilder.forceSucceeded();
					copyRunBuilder.addContext("inactive", "Check is not active and was failing");
					recentCheckRuns.addCheckRun(copyRunBuilder.build());
				}
			} else if ((cachedFor = cachedForMs()) > 0) {
				log.debug("Check is cached for {} more ms", cachedFor);
			}
			return getLastCheckRun();
		}
		log.debug("Starting run()");
		running = true;
		CheckRun.Builder builder = CheckRun.builder(this).unknownIsCritical(isUnknownIsCritical());
		runStartTimestamp = builder.startTime();
		String methodRun = "execute()";
		try {
			ExecutionResult result = executor.execute(builder, runContext);
			builder.result(result);
			methodRun = "evaluate()";
			evaluator.evaluate(builder, null, result);
		} catch (InterruptedException e) {
			builder.timedOut(true).unknown().error(e);
			log.warn("Check got interrupted");
		} catch (Exception e) {
			builder.error(e).critical();
			log.error("{} for check threw an exception", methodRun, e);
		} finally {
			runEndTimestamp = builder.endTime();
			if (builder.getStatus() == Status.SUCCEEDED) {
				clearFailingSince();
				setRestarting(false);
			} else {
				maybeSuppressFailure(builder, setFailingSince(runStartTimestamp));
			}
			builder.failingSince(getFailingSince());
			checkRun = builder.build();
			if (runContext.saveCheckRun()) {
				recentCheckRuns.addCheckRun(checkRun);
			}
			running = false;
			log.debug("Ending run()");
			CheckMDC.remove();
		}
		return checkRun;
	}

	@Override
	public void init(Check previousCheck, @NonNull Config config, @NonNull App app, @NonNull String id) throws Exception {
		if (!initialized) {
			this.config = config;
			this.app = app;
			this.id = id;
			this.fullName = app.getId() + ":" + id;
			CheckMDC.put(this); //need fullname to be set
			if (previousCheck != null && !id.equals(previousCheck.getId())) {
				throw new IllegalArgumentException("Check ids must be equal to initalize from another getCheck");
			}

			if (executor == null) {
				throw new IllegalArgumentException("A CheckExecutor must be set on check " + getFullName());
			}
			if (evaluator == null) {
				throw new IllegalArgumentException("An Evaluator must be set on check" + getFullName());
			}

			if (cacheMs == null) {
				cacheMs = app.getDefaultCacheMs();
			}
			if (timeoutMs == null) {
				timeoutMs = app.getDefaultTimeoutMs();
			}
			if (timeoutMs < 0) {
				RuntimeException e = new IllegalStateException("timeoutMs cannot be less than 0.");
				log.error("timeoutMs is set to {}", timeoutMs, e);
				throw e;
			}
			long hardTimeoutMsLimit = config.getLong("hardTimeoutMsLimit");
			if (timeoutMs > hardTimeoutMsLimit) {
				timeoutMs = hardTimeoutMsLimit;
			}
			long hardCacheMsLimit = config.getLong("hardCacheMsLimit");
			if (cacheMs > hardCacheMsLimit) {
				cacheMs = hardCacheMsLimit;
			}
			if (Duration.ZERO.equals(allowedFailureDuration)) {
				allowedFailureDuration = app.getDefaultAllowedFailureDuration();
			}
			if (allowedFailureDuration == null) {
				allowedFailureDuration = Duration.ZERO;
			}
			if (priority == null) {
				priority = app.getDefaultPriority();
			}
			if (alias == null) {
				alias = id;
			}
			String defaultCheckRunMessage;
			if (!active.isActive()) {
				defaultCheckRunMessage = "Check is not active";
			} else {
				defaultCheckRunMessage = "No checks run since initialization";
			}
			CheckRun defaultCheckRun = CheckRun.builder(this, (CheckRun) null).succeeded().message(defaultCheckRunMessage).build();
			recentCheckRuns.setDefaultCheckRun(defaultCheckRun);
			int recentCheckSizeLimit = config.getInt("recentCheckSizeLimit", 10);
			if (recentCheckRuns.getSizeLimit() > recentCheckSizeLimit) {
				setRecentCheckRunSize(recentCheckSizeLimit);
			}
			try {
				if (previousCheck != null) {
					//order must be preserved
					for (CheckRun checkRun : previousCheck.getRecentCheckRuns()) {
						recentCheckRuns.addCheckRun(checkRun);
					}
					if (previousCheck instanceof Failable) {
						setFailingSince(((Failable) previousCheck).getFailingSince());
					}
					if (previousCheck instanceof Restartable) {
						setRestarting(((Restartable) previousCheck).isRestarting());
					}
					if (previousCheck instanceof Executable) {
						executor.init(((Executable) previousCheck).getExecutor(), this, config);
					}
					if (previousCheck instanceof Clusterable) {
						cluster.init(((Clusterable)previousCheck).getCluster(), this, config);
					}
				} else {
					executor.init(null, this, config);
					cluster.init(null, this, config);
				}
			} catch (Exception e) {
				log.error("Unable to successfully initialize check {} due to error", getFullName(), e);
				throw e;
			}
			if (previousCheck instanceof DefaultCheck) {
				DefaultCheck defaultCheck = (DefaultCheck) previousCheck;
				runEndTimestamp = defaultCheck.runEndTimestamp;
				runStartTimestamp = defaultCheck.runStartTimestamp;
			}
			tags = Collections.unmodifiableSet(tags);
			initialized = true;
			if (isActive()) {
				log.info("Initialized {} {}", getType(), getFullName());
			}
		}
		CheckMDC.remove();
	}

	@Override
	public void close() throws Exception {
		log.debug("Closing {}", getFullName());
		initialized = false;
		recentCheckRuns = null;
		getExecutor().close();
		getCluster().close();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Check) { //implicit null check
			Check that = (Check) obj;
			if (getId() != null
					&& getApp() != null
					&& that.getApp() != null
					&& getId().equals(that.getId())
					&& getApp().getId().equals(that.getApp().getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getApp().getId().hashCode() * 31 + getId().hashCode();
	}

	/**
	 * Checks can be compared to one another. Checks with a higher getPriority() are
	 * first and when tied, checks with a lower getTimeoutMs() break that tie.
	 *
	 * @param check The check to compare this check to
	 * @return
	 */
	@Override
	public int compareTo(Check check) {
		// Sort by priority
		int compare = -Integer.compare(getPriority(), check instanceof Prioritizable ? ((Prioritizable)check).getPriority() : getPriority());
		if (compare == 0) {
			// Then by timeout so that longest timeouts get submitted first
			compare = -Long.compare(getTimeoutMs(), check instanceof Interruptable ? ((Interruptable)check).getTimeoutMs() : getTimeoutMs());
		}
		return compare;
	}
}
