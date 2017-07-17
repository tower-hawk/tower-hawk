package org.towerhawk.spring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.towerhawk.monitor.check.run.CheckRunSelector;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.towerhawk.monitor.check.run.CheckRunSelector.Field.context;
import static org.towerhawk.monitor.check.run.CheckRunSelector.Field.duration;
import static org.towerhawk.monitor.check.run.CheckRunSelector.Field.failingSince;
import static org.towerhawk.monitor.check.run.CheckRunSelector.Field.message;
import static org.towerhawk.monitor.check.run.CheckRunSelector.Field.status;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties("towerhawk.monitor")
@Getter
@Setter
public class Configuration {

	/**
	 * This should be an absolute path to the directory where the configuration definitions live.
	 *
	 * @param checkDefinitionDir
	 */
	private String checkDefinitionDir = "/etc/towerhawk";
	private boolean automaticallyWatchFiles = true;
	private int recentChecksSizeLimit = 10;
	private String defaultHost = "localhost";
	public static final String DEFAULT_LOCAL_HOST = "N/A";
	private String defaultLocalHost = DEFAULT_LOCAL_HOST;
	private long defaultCacheMs = 30000;
	private long defaultTimeoutMs = 30000;
	private byte defaultPriority = 0;
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
	private String defaultJmxHost = "localhost";
	private String mbeanPathSeparator = "|";
	private List<CheckRunSelector.Field> checkRunDefaultFields = null;
	private long JMXConnectionRefreshMs = 600000;
	private int doubleSerializationPrecision = 4;
	private int succeededResponseCode = 200;
	private int unknownResponseCode = 520;
	private int warningResponseCode = 530;
	private int criticalResponseCode = 540;

	public void setCheckRunDefaultFields(List<CheckRunSelector.Field> checkRunDefaultFields) {
		this.checkRunDefaultFields = checkRunDefaultFields;
		//this.checkRunDefaultFields = checkRunDefaultFields.stream().map(s -> Enum.valueOf(CheckRunSelector.Field.class, s)).collect(Collectors.toList());
	}

	@PostConstruct
	public void init() {
		Locale locale = Locale.getDefault();
		if (localeLanguage != null) {
			locale = new Locale(localeLanguage, localeCountry, localeVariant);
		}
		dateTimeFormatter = DateTimeFormatter.ofPattern(defaultDateFormat, locale);
		if (checkRunDefaultFields == null || checkRunDefaultFields.isEmpty()) {
			checkRunDefaultFields = Arrays.asList(status, message, context
				, duration, failingSince);
		}
	}
}
