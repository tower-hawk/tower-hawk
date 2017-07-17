package org.towerhawk.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties("towerhawk.appRunnerThreadPool")
public class ConcurrentAppRunConfiguration extends ConcurrentCheckRunConfiguration {

	public ConcurrentAppRunConfiguration() {
		setThreadGroupName("AppRunner");
		setThreadNamePrefix("AppRunner - ");
	}
}
