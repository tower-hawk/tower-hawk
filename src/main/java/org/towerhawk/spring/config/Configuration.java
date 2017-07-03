package org.towerhawk.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties("towerhawk.monitor")
public class Configuration {

	private String checkDefinitionDir = "/etc/towerhawk";
	private boolean automaticallyWatchFiles = true;
	private int recentChecksSizeLimit = 10;
	private String defaultHost = "localhost";
	public static final String DEFAULT_LOCAL_HOST = "N/A";
	private String defaultLocalHost = "N/A";
	private long defaultCacheMs = 30000;
	private long defaultTimeoutMs = 60000;
	private int defaultPriority = 20;
	private String defaultDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private DateTimeFormatter dateTimeFormatter = null;
	private TimeUnit durationTimeUnit = TimeUnit.MILLISECONDS;
	private String localeLanguage = null;
	private String localeCountry = "";
	private String localeVariant = "";
	private int hardTimeoutLimit = 300000;
	private String lineDelimiter = "\n";
	private boolean runChecksOnStartup = true;
	private boolean runChecksOnRefresh = false;
	private String watcherThreadName = "CheckWatcherThread";
	private boolean prettyPrintResultJson = false;
	private boolean shutdownOnInitializationFailure = true;

	@PostConstruct
	public void init() {
		Locale locale = Locale.getDefault();
		if (localeLanguage != null) {
			locale = new Locale(localeLanguage, localeCountry, localeVariant);
		}
		dateTimeFormatter = DateTimeFormatter.ofPattern(defaultDateFormat, locale);
	}

	public String getCheckDefinitionDir() {
		return checkDefinitionDir;
	}

	/**
	 * This should be an absolute path to the directory where the configuration definitions live.
	 *
	 * @param checkDefinitionDir
	 */
	public void setCheckDefinitionDir(String checkDefinitionDir) {
		this.checkDefinitionDir = checkDefinitionDir;
	}

	public boolean isAutomaticallyWatchFiles() {
		return automaticallyWatchFiles;
	}

	public void setAutomaticallyWatchFiles(boolean automaticallyWatchFiles) {
		this.automaticallyWatchFiles = automaticallyWatchFiles;
	}

	public int getRecentChecksSizeLimit() {
		return recentChecksSizeLimit;
	}

	public void setRecentChecksSizeLimit(int recentChecksSizeLimit) {
		this.recentChecksSizeLimit = recentChecksSizeLimit;
	}

	public String getDefaultHost() {
		return defaultHost;
	}

	public void setDefaultHost(String defaultHost) {
		this.defaultHost = defaultHost;
	}

	public String getDefaultLocalHost() {
		return defaultLocalHost;
	}

	public void setDefaultLocalHost(String defaultLocalHost) {
		this.defaultLocalHost = defaultLocalHost;
	}

	public long getDefaultCacheMs() {
		return defaultCacheMs;
	}

	public void setDefaultCacheMs(long defaultCacheMs) {
		this.defaultCacheMs = defaultCacheMs;
	}

	public long getDefaultTimeoutMs() {
		return defaultTimeoutMs;
	}

	public void setDefaultTimeoutMs(long defaultTimeoutMs) {
		this.defaultTimeoutMs = defaultTimeoutMs;
	}

	public int getDefaultPriority() {
		return defaultPriority;
	}

	public void setDefaultPriority(int defaultPriority) {
		this.defaultPriority = defaultPriority;
	}

	public String getDefaultDateFormat() {
		return defaultDateFormat;
	}

	public void setDefaultDateFormat(String defaultDateFormat) {
		this.defaultDateFormat = defaultDateFormat;
	}

	public TimeUnit getDurationTimeUnit() {
		return durationTimeUnit;
	}

	public void setDurationTimeUnit(TimeUnit durationTimeUnit) {
		this.durationTimeUnit = durationTimeUnit;
	}

	public DateTimeFormatter getDateTimeFormatter() {
		return dateTimeFormatter;
	}

	public void setDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
		this.dateTimeFormatter = dateTimeFormatter;
	}

	public String getLocaleLanguage() {
		return localeLanguage;
	}

	public void setLocaleLanguage(String localeLanguage) {
		this.localeLanguage = localeLanguage;
	}

	public String getLocaleCountry() {
		return localeCountry;
	}

	public void setLocaleCountry(String localeCountry) {
		this.localeCountry = localeCountry;
	}

	public String getLocaleVariant() {
		return localeVariant;
	}

	public void setLocaleVariant(String localeVariant) {
		this.localeVariant = localeVariant;
	}

	public int getHardTimeoutLimit() {
		return hardTimeoutLimit;
	}

	public void setHardTimeoutLimit(int hardTimeoutLimit) {
		this.hardTimeoutLimit = hardTimeoutLimit;
	}

	public String getLineDelimiter() {
		return lineDelimiter;
	}

	public void setLineDelimiter(String lineDelimiter) {
		this.lineDelimiter = lineDelimiter;
	}

	public boolean isRunChecksOnStartup() {
		return runChecksOnStartup;
	}

	public void setRunChecksOnStartup(boolean runChecksOnStartup) {
		this.runChecksOnStartup = runChecksOnStartup;
	}

	public boolean isRunChecksOnRefresh() {
		return runChecksOnRefresh;
	}

	public void setRunChecksOnRefresh(boolean runChecksOnRefresh) {
		this.runChecksOnRefresh = runChecksOnRefresh;
	}

	public String getWatcherThreadName() {
		return watcherThreadName;
	}

	public void setWatcherThreadName(String watcherThreadName) {
		this.watcherThreadName = watcherThreadName;
	}

	public boolean isPrettyPrintResultJson() {
		return prettyPrintResultJson;
	}

	public void setPrettyPrintResultJson(boolean prettyPrintResultJson) {
		this.prettyPrintResultJson = prettyPrintResultJson;
	}

	public boolean isShutdownOnInitializationFailure() {
		return shutdownOnInitializationFailure;
	}

	public void setShutdownOnInitializationFailure(boolean shutdownOnInitializationFailure) {
		this.shutdownOnInitializationFailure = shutdownOnInitializationFailure;
	}
}
