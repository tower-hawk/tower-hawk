package org.towerhawk.monitor.check.impl;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.active.Active;
import org.towerhawk.monitor.active.Enabled;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
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
public abstract class AbstractCheck implements Check {

	protected String type;
	private String id = null;
	protected long runEndTimestamp = 0;
	protected long runStartTimestamp = 0;
	protected long cacheMs = Check.CACHE_MS;
	protected long timeoutMs = Check.TIMEOUT_MS;
	protected int priority = Check.PRIORITY;
	protected ZonedDateTime failingSince = null;
	protected String alias = null;
	private App app = null;
	protected Set<String> tags = new LinkedHashSet<>();
	private Active active = new Enabled();
	private RecentCheckRun recentCheckRuns = new RecentCheckRun();
	private boolean running = false;
	private boolean unknownIsCritical = true;
	protected Configuration configuration;
	private boolean initialized = false;
	private CheckRun.Builder builder = null;
	protected Threshold threshold;

	@Override
	public final boolean isActive() {
		return initialized && active.isActive();
	}

	protected void setActive(Active active) {
		this.active = active;
	}

	@Override
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
		return System.currentTimeMillis() - runEndTimestamp < cacheMs;
	}

	protected final long cachedFor() {
		return cacheMs - (System.currentTimeMillis() - runEndTimestamp);
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
	public final synchronized CheckRun run() {
		log.debug("Starting run() for {}", getId());
		CheckRun checkRun;
		if (!canRun()) {
			if (running) {
				log.debug("Check {} is already running", getId());
			} else if (!initialized) {
				log.warn("Trying to run check {} but it is not initialized", getId());
			}	else if (!isActive()) {
				log.debug("Check {} is not active", getId());
				CheckRun lastRun = recentCheckRuns.getLastRun();
				if (lastRun.getStatus() != CheckRun.Status.SUCCEEDED) {
					CheckRun.Builder copyRunBuilder = CheckRun.builder(lastRun);
					copyRunBuilder.succeeded();
					copyRunBuilder.addContext("inactive", "Check is not active and was failing");
					recentCheckRuns.addCheckRun(copyRunBuilder.build());
				}
			} else if (isCached()) {
				log.debug("Check {} is cached for {} more ms", getId(), cachedFor());
			}
			return recentCheckRuns.getLastRun();
		}
		running = true;
		builder = CheckRun.builder(this);
		builder.unknownIsCritical(isUnknownIsCritical());
		runStartTimestamp = builder.startTime();
		try {
			doRun(builder);
		} catch (InterruptedException e) {
			builder.timedOut(true).unknown().error(e);
			log.warn("Check {} got interrupted", getId());
		} catch (Exception e) {
			builder.error(e);
			log.error("doRun() for check {} threw an exception", getId(), e);
		} finally {
			runEndTimestamp = builder.endTime();
			if (builder.getStatus() == CheckRun.Status.SUCCEEDED) {
				clearFailingSince();
			} else {
				setFailingSince(runStartTimestamp);
			}
			builder.failingSince(failingSince);
			checkRun = builder.build();
			recentCheckRuns.addCheckRun(checkRun);
			runStartTimestamp = 0;
			running = false;
			log.debug("Ending run() for {}", getId());
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
				check.getRecentCheckRuns().forEach(checkRun -> recentCheckRuns.addCheckRun(checkRun));
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
	protected abstract void doRun(CheckRun.Builder builder) throws InterruptedException;

}
