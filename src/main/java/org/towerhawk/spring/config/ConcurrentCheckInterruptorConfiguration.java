package org.towerhawk.spring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@org.springframework.context.annotation.Configuration
@ConfigurationProperties("towerhawk.interruptor")
public class ConcurrentCheckInterruptorConfiguration {

	private String threadName = "ConcurrentCheckInterruptorThread";
	private int priority = 6;
	private long pollMs = 10000;

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public long getPollMs() {
		return pollMs;
	}

	public void setPollMs(long pollMs) {
		this.pollMs = pollMs;
	}
}
