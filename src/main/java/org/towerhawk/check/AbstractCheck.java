package org.towerhawk.check;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.check.active.ActiveCheck;
import org.towerhawk.check.active.AlwaysActive;
import org.towerhawk.check.app.App;
import org.towerhawk.check.run.CheckRun;
import org.towerhawk.check.run.CheckRunImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractCheck implements Check {

	private static final Logger log = LoggerFactory.getLogger(AbstractCheck.class);

	protected String type;
	private String id = null;
	protected long runTimestamp = 0;
	protected long cacheMs = Check.CACHE_MS;
	protected long timeoutMs = Check.TIMEOUT_MS;
	protected long retryIntervalMs = Check.RETRY_INTERVAL_MS;
	protected long consecutiveFailures = Check.CONSECUTIVE_FAILURES;
	protected int priority = Check.PRIORITY;
	protected String alias = null;
	private App app = null;
	protected Set<String> tags = new LinkedHashSet<>();
	private ActiveCheck activeCheck = null;
	@JsonIgnore
	protected RecentCheckRun recentChecks = new RecentCheckRun();
	protected boolean running = false;
	protected long runningStartTimeMs = 0;
	private boolean enabled = true;

	@Override
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
	public long getRetryIntervalMs() {
		return retryIntervalMs;
	}

	@Override
	public long getConsecutiveFailuresToAlert() {
		return consecutiveFailures;
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
	public Collection<String> getTags() {
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

	@Override
	public long getRunningStartTimeMs() {
		return runningStartTimeMs;
	}

	@Override
	public final boolean isEnabled() {
		return enabled;
	}

	@Override
	public final CheckRun run() {
		log.debug("Running getCheck {}", id);
		runningStartTimeMs = System.currentTimeMillis();
		if (!isEnabled() || !isActive() || runningStartTimeMs - runTimestamp < cacheMs) {
			log.debug("Not running getCheck because either isActive returned {} or {} ms since last execution is < {}", isActive(), runningStartTimeMs - runTimestamp, cacheMs);
			//TODO work out this logic better so that if a getCheck has become disabled or inactive
			//TODO that it doesn't continually alert.
			CheckRun checkRun = recentChecks.getLastRun();
			if (checkRun != null && checkRun.getStatus() == CheckRun.Status.SUCCEEDED) {
				return checkRun;
			} else {
				return null;
			}
		}
		running = true;
		CheckRun.Builder checkRunBuilder = CheckRun.builder(this);
		long startNano = System.nanoTime();
		try {
			doRun(checkRunBuilder);
		} catch (InterruptedException e) {
			checkRunBuilder.timedOut(true);
			checkRunBuilder.unknown();
			checkRunBuilder.error(e);
			checkRunBuilder.consecutiveFailures(++this.consecutiveFailures);
			log.warn("Check {} got interrupted", id);
		}
		long endNano = System.nanoTime();
		runTimestamp = System.currentTimeMillis();
		checkRunBuilder.startTime(runningStartTimeMs);
		checkRunBuilder.endTime(runTimestamp);
		checkRunBuilder.durationNanos(endNano - startNano);
		CheckRun run = checkRunBuilder.build();
		recentChecks.addCheckRun(run);
		runningStartTimeMs = 0;
		running = false;
		log.debug("Ending getCheck {}", id);
		return run;
	}

	@Override
	public void init(Check check) {
		if (id == null) {
			throw new IllegalStateException("id must be set before calling init");
		}
		if (check != null && !id.equals(check.getId())) {
			throw new IllegalArgumentException("Check ids must be equal to initalize from another getCheck");
		}
		if (check != null && check instanceof AbstractCheck) {
			setRecentChecks(((AbstractCheck) check).getRecentChecks());
		}
		if (cacheMs == Check.CACHE_MS) {
			cacheMs = app.getDefaultCacheMs();
		}
		if (timeoutMs == Check.TIMEOUT_MS) {
			timeoutMs = app.getDefaultTimeoutMs();
		}
		if (retryIntervalMs == Check.RETRY_INTERVAL_MS) {
			retryIntervalMs = app.getDefaultRetryIntervalMs();
		}
		if (consecutiveFailures == Check.CONSECUTIVE_FAILURES) {
			consecutiveFailures = app.getDefaultConsecutiveFailures();
		}
		if (priority == Check.PRIORITY) {
			priority = app.getDefaultPriority();
		}
		if (alias == null) {
			alias = id;
		}
		if (activeCheck == null) {
			activeCheck = new AlwaysActive();
		}
		log.debug("Initialized getCheck {}", id);
	}

	@Override
	public void close() throws IOException {
		log.debug("Closing getCheck {}", id);
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

	@Override
	public int compareTo(Check check) {
		// Sort by priority
		int compare = Integer.compare(priority, check.getPriority());
		if (compare != 0) {
			return compare;
		}
		// Then by timeout so that shortest timeouts get submitted first
		return Long.compare(timeoutMs, check.getTimeoutMs());
	}

	private RecentCheckRun getRecentChecks() {
		return recentChecks;
	}

	private void setRecentChecks(RecentCheckRun recentChecks) {
		this.recentChecks = recentChecks;
	}

	protected String transformInputStream(InputStream inputStream) {
		return new BufferedReader(new InputStreamReader(inputStream))
			.lines().collect(Collectors.joining("\n"));
	}

	/**
	 * This is the method each concrete class should implement
	 * The builder is retained by the abstract class and used to
	 * set values like getDuration, getStartTime, getEndTime, and
	 * other things that should be handled in a standard way.
	 *
	 * @param checkRunBuilder
	 */
	protected abstract void doRun(CheckRunImpl.Builder checkRunBuilder) throws InterruptedException;

}
