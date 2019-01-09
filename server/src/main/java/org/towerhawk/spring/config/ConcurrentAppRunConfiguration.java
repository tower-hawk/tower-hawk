package org.towerhawk.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("towerhawk.appRunnerThreadPool")
public class ConcurrentAppRunConfiguration extends ConcurrentCheckRunConfiguration {

	public ConcurrentAppRunConfiguration() {
		setThreadGroupName("AppRunner");
		setThreadNamePrefix("AppRunner - ");
	}
}
