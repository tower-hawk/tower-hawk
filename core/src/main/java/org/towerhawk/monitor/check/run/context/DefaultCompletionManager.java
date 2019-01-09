package org.towerhawk.monitor.check.run.context;

import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.Check;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class DefaultCompletionManager implements CompletionManager {

	private CountDownLatch countDownLatch;
	private Map<String, Map<String, Check>> checks = new ConcurrentHashMap<>();

	public DefaultCompletionManager(int apps) {
		countDownLatch = new CountDownLatch(apps);
	}

	@Override
	public void registerChecks(String appId, Map<String, Check> checks) {
		this.checks.put(appId, checks);
		countDownLatch.countDown();
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			log.warn("Interrupted while waiting for registration finalization");
		}
	}
}
