package org.towerhawk.spring.autoConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.towerhawk.monitor.check.run.concurrent.ConcurrentCheckInterruptor;
import org.towerhawk.monitor.check.run.concurrent.ConcurrentCheckRunner;
import org.towerhawk.spring.config.ConcurrentAppRunConfiguration;
import org.towerhawk.spring.config.ConcurrentCheckInterruptorConfiguration;
import org.towerhawk.spring.config.ConcurrentCheckRunConfiguration;

import java.util.concurrent.ExecutorService;

@org.springframework.context.annotation.Configuration
public class CheckServiceAutoConfiguration {

	@Bean
	public ThreadPoolExecutorFactoryBean checkRunService(ConcurrentCheckRunConfiguration config) {
		return getThreadPoolExecutorFactoryBean(config, config.getThreadGroupName(), config.getThreadNamePrefix());
	}

	@Bean
	public ThreadPoolExecutorFactoryBean appRunService(ConcurrentAppRunConfiguration config) {
		return getThreadPoolExecutorFactoryBean(config, config.getThreadGroupName(), config.getThreadNamePrefix());
	}

	@Bean
	public ThreadPoolExecutorFactoryBean monitorRunService(ConcurrentAppRunConfiguration config) {
		return getThreadPoolExecutorFactoryBean(config, "MonitorService", "MonitorService - ");
	}

	private ThreadPoolExecutorFactoryBean getThreadPoolExecutorFactoryBean(ConcurrentCheckRunConfiguration config, String threadGroupName, String threadNamePrefix) {
		ThreadPoolExecutorFactoryBean t = new ThreadPoolExecutorFactoryBean();
		t.setCorePoolSize(config.getCorePoolSize());
		t.setMaxPoolSize(Math.max(config.getMaxPoolSize(), config.getCorePoolSize()));
		t.setQueueCapacity(config.getQueueCapacity());
		t.setKeepAliveSeconds(config.getKeepAliveSeconds());
		t.setAllowCoreThreadTimeOut(config.isAllowCoreTimeout());
		t.setThreadGroupName(threadGroupName);
		t.setThreadNamePrefix(threadNamePrefix);
		t.setWaitForTasksToCompleteOnShutdown(config.isWaitForTasksToCompleteOnShutdown());
		return t;
	}

	@Bean
	public ConcurrentCheckInterruptor interruptor(ConcurrentCheckInterruptorConfiguration configuration) {
		return new ConcurrentCheckInterruptor(configuration);
	}

	@Bean
	public ConcurrentCheckRunner checkCheckRunner(
			ConcurrentCheckInterruptor interruptor,
			ExecutorService checkRunService
	) {
		return new ConcurrentCheckRunner(interruptor, checkRunService);
	}

	@Bean
	public ConcurrentCheckRunner appCheckRunner(
			ConcurrentCheckInterruptor interruptor,
			ExecutorService appRunService
	) {
		return new ConcurrentCheckRunner(interruptor, appRunService);
	}

	@Bean
	public ConcurrentCheckRunner monitorCheckRunner(
			ConcurrentCheckInterruptor interruptor,
			ExecutorService monitorRunService
	) {
		return new ConcurrentCheckRunner(interruptor, monitorRunService);
	}
}
