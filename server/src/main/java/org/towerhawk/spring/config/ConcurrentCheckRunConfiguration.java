package org.towerhawk.spring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.towerhawk.config.AbstractReflectiveConfig;

@Getter
@Setter
@Configuration
@ConfigurationProperties("towerhawk.checkRunnerThreadPool")
@Primary
public class ConcurrentCheckRunConfiguration extends AbstractReflectiveConfig {

	private int corePoolSize = Runtime.getRuntime().availableProcessors();
	private int maxPoolSize = corePoolSize * 4;
	private int queueCapacity = corePoolSize * 1000;
	private int keepAliveSeconds = 600;
	private boolean allowCoreTimeout = true;
	private String threadGroupName = "CheckRunner";
	private String threadNamePrefix = "CheckRunner - ";
	private boolean waitForTasksToCompleteOnShutdown = false;
}
