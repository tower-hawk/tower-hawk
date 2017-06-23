package org.towerhawk.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

@org.springframework.context.annotation.Configuration
public class CheckServiceAutoConfiguration {

	@Bean
	public ThreadPoolExecutorFactoryBean checkRunService(CheckRunnerThreadPoolConfiguration config) {
		return getCheckRunnerExecutor(config);
	}

	@Bean
	public ThreadPoolExecutorFactoryBean checkRunInterruptor(CheckRunnerThreadPoolConfiguration config) {
		return getCheckRunnerExecutor(config);
	}

	private ThreadPoolExecutorFactoryBean getCheckRunnerExecutor(CheckRunnerThreadPoolConfiguration config) {
		ThreadPoolExecutorFactoryBean threadPoolExecutor = new ThreadPoolExecutorFactoryBean();
		threadPoolExecutor.setCorePoolSize(config.getCorePoolSize());
		threadPoolExecutor.setMaxPoolSize(config.getMaxPoolSize());
		threadPoolExecutor.setQueueCapacity(config.getQueueCapacity());
		threadPoolExecutor.setKeepAliveSeconds(config.getKeepAliveSeconds());
		return threadPoolExecutor;
	}

}
