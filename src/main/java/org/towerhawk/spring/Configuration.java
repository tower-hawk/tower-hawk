package org.towerhawk.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties("towerhawk.monitor")
public class Configuration {

	private static Configuration __configuration;
	private String checkDefinitionDir = "/etc/towerhawk";
	private int recentChecksSizeLimit = 10;
	private String defaultHost = "localhost";
	public static final String DEFAULT_LOCAL_HOST = "N/A";
	private String defaultLocalHost = "N/A";

	@PostConstruct
	private void init() {
		__configuration = this;
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
}
