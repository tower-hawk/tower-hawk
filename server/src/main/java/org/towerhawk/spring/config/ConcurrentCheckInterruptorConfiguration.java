package org.towerhawk.spring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.towerhawk.config.AbstractReflectiveConfig;

@Configuration
@ConfigurationProperties("towerhawk.interruptor")
@Setter
@Getter
public class ConcurrentCheckInterruptorConfiguration extends AbstractReflectiveConfig {

	private String threadName = "ConcurrentCheckInterruptorThread";
	private int priority = 6;
	private long pollMs = 10000;
}
