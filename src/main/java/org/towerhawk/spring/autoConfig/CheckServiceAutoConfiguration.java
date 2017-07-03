package org.towerhawk.spring.autoConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.towerhawk.spring.config.ConcurrentAppRunConfiguration;
import org.towerhawk.spring.config.ConcurrentCheckRunConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@org.springframework.context.annotation.Configuration
public class CheckServiceAutoConfiguration {

	@Bean
	public ThreadPoolExecutorFactoryBean checkRunService(ConcurrentCheckRunConfiguration config) {
		return getThreadPoolExecutorFactoryBean(config);
	}

	@Bean
	public ThreadPoolExecutorFactoryBean appRunService(ConcurrentAppRunConfiguration config) {
		return getThreadPoolExecutorFactoryBean(config);
	}

	private ThreadPoolExecutorFactoryBean getThreadPoolExecutorFactoryBean(ConcurrentCheckRunConfiguration config) {
		ThreadPoolExecutorFactoryBean threadPoolExecutor = new ThreadPoolExecutorFactoryBean();
		threadPoolExecutor.setCorePoolSize(config.getCorePoolSize());
		threadPoolExecutor.setMaxPoolSize(config.getMaxPoolSize());
		threadPoolExecutor.setQueueCapacity(config.getQueueCapacity());
		threadPoolExecutor.setKeepAliveSeconds(config.getKeepAliveSeconds());
		return threadPoolExecutor;
	}
}
