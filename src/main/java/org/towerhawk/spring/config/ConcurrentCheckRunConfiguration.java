package org.towerhawk.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties("towerhawk.checkRunnerThreadPool")
@Primary
public class ConcurrentCheckRunConfiguration {

	private int corePoolSize = Runtime.getRuntime().availableProcessors();
	private int maxPoolSize = corePoolSize * 4;
	private int queueCapacity = corePoolSize * 1000;
	private int keepAliveSeconds = 60;

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}

	public void setKeepAliveSeconds(int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}
}
