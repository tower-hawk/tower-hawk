package org.towerhawk.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties("towerhawk.monitor")
public class Configuration {

	private static Configuration __configuration;
	private String checkDefinitionDir = "/etc/towerhawk";
	private boolean automaticallyWatchFiles = true;
	private int recentChecksSizeLimit = 10;
	private String defaultHost = "localhost";
	public static final String DEFAULT_LOCAL_HOST = "N/A";
	private String defaultLocalHost = "N/A";
	private long defaultCacheMs = 30000;
	private long defaultTimeoutMs = 60000;
	private long defaultRetryIntervalMs = 60000;
	private long defaultConsecutiveFailures = 1;
	private int defaultPriority = 20;
	private String defaultDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private DateTimeFormatter dateTimeFormatter = null;
	private String localeLanguage = null;
	private String localeCountry = "";
	private String localeVariant = "";
	private int hardTimeoutLimit = 300000;

	@PostConstruct
	private void init() {
		__configuration = this;
		Locale locale = Locale.getDefault();
		if (localeLanguage != null) {
			locale = new Locale(localeLanguage, localeCountry, localeVariant);
		}
		dateTimeFormatter = DateTimeFormatter.ofPattern(defaultDateFormat, locale);
	}

	public static Configuration get() {
		return __configuration;
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

	public long getDefaultRetryIntervalMs() {
		return defaultRetryIntervalMs;
	}

	public void setDefaultRetryIntervalMs(long defaultRetryIntervalMs) {
		this.defaultRetryIntervalMs = defaultRetryIntervalMs;
	}

	public long getDefaultConsecutiveFailures() {
		return defaultConsecutiveFailures;
	}

	public void setDefaultConsecutiveFailures(long defaultConsecutiveFailures) {
		this.defaultConsecutiveFailures = defaultConsecutiveFailures;
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
}
