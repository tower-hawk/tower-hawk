package org.towerhawk.spring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

@Getter
@Setter
@org.springframework.context.annotation.Configuration
@ConfigurationProperties("towerhawk.checkRunnerThreadPool")
@Primary
public class ConcurrentCheckRunConfiguration {

	private int corePoolSize = Runtime.getRuntime().availableProcessors();
	private int maxPoolSize = corePoolSize * 4;
	private int queueCapacity = corePoolSize * 1000;
	private int keepAliveSeconds = 60;
	private boolean allowCoreTimeout = true;
	private String threadGroupName = "CheckRunner";
	private String threadNamePrefix = "CheckRunner - ";
	private boolean waitForTasksToCompleteOnShutdown = false;
}
