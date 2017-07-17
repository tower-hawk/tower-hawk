package org.towerhawk.monitor.check.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.active.Active;
import org.towerhawk.monitor.active.Enabled;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.CheckContext;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.threshold.Threshold;
import org.towerhawk.spring.config.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public abstract class AbstractCheck implements Check {

	@Setter
	private String type;
	private String id = null;
	protected transient String fullName = null;
	private long runEndTimestamp = 0;
	private long runStartTimestamp = 0;
	@Setter
	private long cacheMs = Check.CACHE_MS;
	@Setter
	private long timeoutMs = Check.TIMEOUT_MS;
	@Setter
	private byte priority = Check.PRIORITY;
	private ZonedDateTime failingSince = null;
	private String alias = null;
	@JsonIgnore
	private App app = null;
	private Set<String> tags = new LinkedHashSet<>();
	private Active active = new Enabled();
	@JsonIgnore
	private RecentCheckRun recentCheckRuns = new RecentCheckRun();
	private boolean running = false;
	private boolean unknownIsCritical = true;
	@Setter
	@JsonIgnore
	private Configuration configuration;
	private boolean initialized = false;
	@JsonIgnore
	private CheckRun.Builder builder = null;
	@Setter
	private Threshold threshold;

	@Override
	public final boolean isActive() {
		return initialized && active.isActive();
	}

	protected void setActive(Active active) {
		this.active = active;
	}

	@Override
	@JsonIgnore
	public CheckRun getLastCheckRun() {
		return recentCheckRuns.getLastRun();
	}

	@Override
	public List<CheckRun> getRecentCheckRuns() {
		return recentCheckRuns.getRecentCheckRuns();
	}

	protected boolean isUnknownIsCritical() {
		return unknownIsCritical;
	}

	@Override
	public final boolean isCached() {
		return cachedFor() > 0;
	}

	protected final long cachedFor() {
		return getCacheMs() - (System.currentTimeMillis() - runEndTimestamp);
	}

	protected int getMsRemaining(boolean throwException) {
		long timeRemaining = getTimeoutMs() - (System.currentTimeMillis() - runStartTimestamp);
		if (timeRemaining < 0 && throwException) {
			throw new IllegalStateException("Check is timed out");
		}
		return (int) timeRemaining;
	}

	protected final void setFailingSince(long epochMillis) {
		setFailingSince(ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()));
	}

	protected final void setFailingSince() {
		setFailingSince(ZonedDateTime.now());
	}

	protected final void setFailingSince(ZonedDateTime failingSince) {
		if (this.failingSince == null) {
			this.failingSince = failingSince;
		}
	}

	protected final void clearFailingSince() {
		failingSince = null;
	}

	protected final void setRecentCheckRunSize(int size) {
		recentCheckRuns.setSizeLimit(size);
	}

	@Override
	@Synchronized
	public final CheckRun run(CheckContext checkContext) {
		CheckRun checkRun;
		if (!checkContext.shouldRun() || !canRun()) {
			if (running) {
				log.debug("Check {} is already running", getFullName());
			} else if (!initialized) {
				log.warn("Trying to run check {} but it is not initialized", getFullName());
			} else if (!isActive()) {
				log.debug("Check {} is not active", getFullName());
				CheckRun lastRun = getLastCheckRun();
				if (lastRun.getStatus() != CheckRun.Status.SUCCEEDED) {
					CheckRun.Builder copyRunBuilder = CheckRun.builder(lastRun);
					copyRunBuilder.succeeded();
					copyRunBuilder.addContext("inactive", "Check is not active and was failing");
					recentCheckRuns.addCheckRun(copyRunBuilder.build());
				}
			} else if (isCached()) {
				log.debug("Check {} is cached for {} more ms", getFullName(), cachedFor());
			}
			return getLastCheckRun();
		}
		log.debug("Starting run() for {}", getFullName());
		running = true;
		builder = CheckRun.builder(this).unknownIsCritical(isUnknownIsCritical());
		runStartTimestamp = builder.startTime();
		try {
			doRun(builder, checkContext);
		} catch (InterruptedException e) {
			builder.timedOut(true).unknown().error(e);
			log.warn("Check {} got interrupted", getFullName());
		} catch (Exception e) {
			builder.error(e).critical();
			log.error("doRun() for check {} threw an exception", getFullName(), e);
		} finally {
			runEndTimestamp = builder.endTime();
			if (builder.getStatus() == CheckRun.Status.SUCCEEDED) {
				clearFailingSince();
			} else {
				setFailingSince(runStartTimestamp);
			}
			builder.failingSince(failingSince);
			checkRun = builder.build();
			if (checkContext.saveCheckRun()) {
				recentCheckRuns.addCheckRun(checkRun);
			}
			running = false;
			log.debug("Ending run() for {}", getFullName());
		}
		return checkRun;
	}

	@Override
	public void init(Check check, @NonNull Configuration configuration, @NonNull App app, @NonNull String id) {
		if (!initialized) {
			if (check != null && !id.equals(check.getId())) {
				throw new IllegalArgumentException("Check ids must be equal to initalize from another getCheck");
			}
			this.configuration = configuration;
			this.app = app;
			this.id = id;
			this.fullName = app.getId() + ":" + id;
			if (cacheMs == Check.CACHE_MS) {
				cacheMs = app.getDefaultCacheMs();
			}
			if (timeoutMs == Check.TIMEOUT_MS) {
				timeoutMs = app.getDefaultTimeoutMs();
			}
			if (timeoutMs < 0) {
				RuntimeException e = new IllegalStateException("timeoutMs cannot be less than 0.");
				log.error("timeoutMs is set to {}", timeoutMs, e);
				throw e;
			}
			if (timeoutMs > configuration.getHardTimeoutLimit()) {
				timeoutMs = configuration.getHardTimeoutLimit();
			}
			if (priority == Check.PRIORITY) {
				priority = app.getDefaultPriority();
			}
			if (alias == null) {
				alias = id;
			}
			String defaultCheckRunMessage;
			if (!isActive()) {
				defaultCheckRunMessage = "Check is not active";
			} else {
				defaultCheckRunMessage = "No checks run since initialization";
			}
			CheckRun defaultCheckRun = CheckRun.builder(this, null).succeeded().message(defaultCheckRunMessage).build();
			recentCheckRuns.setDefaultCheckRun(defaultCheckRun);
			this.setRecentCheckRunSize(configuration.getRecentChecksSizeLimit());
			if (check != null) {
				//order must be preserved
				for (CheckRun checkRun : check.getRecentCheckRuns()) {
					recentCheckRuns.addCheckRun(checkRun);
				}
				failingSince = check.getFailingSince();
			}
			if (check instanceof AbstractCheck) {
				AbstractCheck abstractCheck = (AbstractCheck) check;
				runEndTimestamp = abstractCheck.runEndTimestamp;
				runStartTimestamp = abstractCheck.runStartTimestamp;
			}
			tags = Collections.unmodifiableSet(tags);
			initialized = true;
			log.debug("Initialized check {}", id);
		}
	}

	@Override
	public void close() throws IOException {
		log.debug("Closing check {}", id);
		recentCheckRuns = null;
		builder = null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Check) { //implicit null check
			Check that = (Check) obj;
			if (id != null
				&& app != null
				&& that.getApp() != null
				&& id.equals(that.getId())
				&& app.getId().equals(that.getApp().getId())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return app.getId().hashCode() * 31 + id.hashCode();
	}

	protected String transformInputStream(InputStream inputStream) {
		return new BufferedReader(new InputStreamReader(inputStream))
			.lines().collect(Collectors.joining(configuration.getLineDelimiter()));
	}

	/**
	 * This is the method each concrete class should implement
	 * The builder is retained by the abstract class and used to
	 * set values like getDuration, getStartTime, getEndTime, and
	 * other things that should be handled in a standard way.
	 *
	 * @param builder
	 */
	protected abstract void doRun(CheckRun.Builder builder, CheckContext context) throws InterruptedException;

}
