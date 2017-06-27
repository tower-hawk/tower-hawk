package org.towerhawk.check;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.towerhawk.check.app.App;
import org.towerhawk.check.run.CheckRun;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
public interface Check extends Comparable<Check>, Closeable {

	long CACHE_MS = -1;
	long TIMEOUT_MS = -1;
	long RETRY_INTERVAL_MS = -1;
	long CONSECUTIVE_FAILURES = -1;
	int PRIORITY = -1;

	String getId();

	void setId(String id);

	CheckRun run();

	boolean isActive();

	long getCacheMs();

	long getTimeoutMs();

	long getRetryIntervalMs();

	long getConsecutiveFailuresToAlert();

	int getPriority();

	String getAlias();

	String getType();

	App getApp();

	void setApp(App app);

	Collection<String> getTags();

	CheckRun getLastCheckRun();

	List<CheckRun> getRecentCheckRuns();

	boolean isRunning();

	long getRunningStartTimeMs();

	void init(Check check);

	boolean isEnabled();
}
