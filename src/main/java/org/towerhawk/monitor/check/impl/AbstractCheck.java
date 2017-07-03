package org.towerhawk.monitor.check.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.monitor.active.ActiveCheck;
import org.towerhawk.monitor.active.Enabled;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRun;
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

public abstract class AbstractCheck implements Check {

	private static final Logger log = LoggerFactory.getLogger(AbstractCheck.class);

	protected String type;
	private String id = null;
	protected long runEndTimestamp = 0;
	protected long runStartTimestamp = 0;
	protected long cacheMs = Check.CACHE_MS;
	protected long timeoutMs = Check.TIMEOUT_MS;
	protected ZonedDateTime failingSince = null;
	protected int priority = Check.PRIORITY;
	protected String alias = null;
	private App app = null;
	protected Set<String> tags = new LinkedHashSet<>();
	private ActiveCheck activeCheck = new Enabled();
	@JsonIgnore
	protected RecentCheckRun recentChecks;
	protected boolean running = false;
	private boolean unknownIsCritical = true;
	protected Configuration configuration;
	private boolean initialized = false;
	private CheckRun.Builder checkRunBuilder = null;

	public final String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public long getCacheMs() {
		return cacheMs;
	}

	@Override
	public long getTimeoutMs() {
		return timeoutMs;
	}

	@Override
	public ZonedDateTime getFailingSince() {
		return failingSince;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public final App getApp() {
		return app;
	}

	@Override
	public void setApp(App app) {
		this.app = app;
	}

	@Override
	public Set<String> getTags() {
		return Collections.unmodifiableSet(tags);
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public final boolean isActive() {
		return activeCheck.isActive();
	}

	public void setActiveCheck(ActiveCheck activeCheck) {
		this.activeCheck = activeCheck;
	}

	@Override
	public CheckRun getLastCheckRun() {
		return recentChecks.getLastRun();
	}

	@Override
	public List<CheckRun> getRecentCheckRuns() {
		return recentChecks.getRecentCheckRuns();
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	public boolean isUnknownIsCritical() {
		return unknownIsCritical;
	}

	@Override
	public final boolean isCached() {
		return System.currentTimeMillis() - runEndTimestamp < cacheMs;
	}

	public final long cachedFor() {
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

	@Override
	public final synchronized CheckRun run() {
		log.debug("Starting run() for {}", id);
		CheckRun checkRun;
		if (!initialized) {
			log.warn("Trying to run check before it is initialized");
			return recentChecks.getLastRun();
		} else if (!canRun()) {
			if (running) {
				log.debug("Check {} is already running", getId());
			} else if (!isActive()) {
				log.debug("Check {} is not active", getId());
				CheckRun lastRun = recentChecks.getLastRun();
				if (lastRun.getStatus() != CheckRun.Status.SUCCEEDED) {
					CheckRun.Builder copyRunBuilder = CheckRun.builder(lastRun);
					copyRunBuilder.succeeded();
					copyRunBuilder.addContext("inactive", "Check is not active and was failing");
					recentChecks.addCheckRun(copyRunBuilder.build());
				}
			} else if (isCached()) {
				log.debug("Check {} is cached for {} more ms", getId(), cachedFor());
			}
			return recentChecks.getLastRun();
		}
		running = true;
		checkRunBuilder = CheckRun.builder(this);
		runStartTimestamp = checkRunBuilder.startTime();
		try {
			doRun(checkRunBuilder);
		} catch (InterruptedException e) {
			checkRunBuilder.timedOut(true).unknown().error(e);
			log.warn("Check {} got interrupted", id);
		} catch (Exception e) {
			checkRunBuilder.error(e);
			log.error("doRun() for check {} threw an exception", getId(), e);
		} finally {
			runEndTimestamp = checkRunBuilder.endTime();
			if (checkRunBuilder.getStatus() == CheckRun.Status.SUCCEEDED) {
				failingSince = null;
			} else {
				setFailingSince(runStartTimestamp);
			}
			checkRunBuilder.failingSince(failingSince);
			checkRun = checkRunBuilder.build();
			recentChecks.addCheckRun(checkRun);
			runStartTimestamp = 0;
			running = false;
			log.debug("Ending run() for {}", id);
		}
		return checkRun;
	}

	@Override
	public void init(Check check, Configuration configuration) {
		if (!initialized) {
			this.configuration = configuration;
			if (id == null) {
				throw new IllegalStateException("id must be set before calling init");
			}
			if (check != null && !id.equals(check.getId())) {
				throw new IllegalArgumentException("Check ids must be equal to initalize from another getCheck");
			}
			if (cacheMs == Check.CACHE_MS) {
				cacheMs = app.getDefaultCacheMs();
			}
			if (timeoutMs == Check.TIMEOUT_MS) {
				timeoutMs = app.getDefaultTimeoutMs();
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
			if (check != null) {
				check.getRecentCheckRuns().forEach(checkRun -> recentChecks.addCheckRun(checkRun));
				if (check.getLastCheckRun() != null) {
					defaultCheckRun = check.getLastCheckRun();
				}
			}
			recentChecks = new RecentCheckRun(configuration.getRecentChecksSizeLimit(), defaultCheckRun);
			initialized = true;
			log.debug("Initialized check {}", id);
		}
	}

	@Override
	public void close() throws IOException {
		log.debug("Closing check {}", id);
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

	private RecentCheckRun getRecentChecks() {
		return recentChecks;
	}

	private void setRecentChecks(RecentCheckRun recentChecks) {
		this.recentChecks = recentChecks;
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
	 * @param checkRunBuilder
	 */
	protected abstract void doRun(CheckRun.Builder checkRunBuilder) throws InterruptedException;

}
