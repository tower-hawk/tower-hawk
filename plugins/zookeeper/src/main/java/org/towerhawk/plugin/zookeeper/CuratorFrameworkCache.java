package org.towerhawk.plugin.zookeeper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.data.Stat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CuratorFrameworkCache {

	private static Map<String, CuratorFramework> cacheMap = new ConcurrentHashMap<>();
	private static RetryPolicy retryPolicy = new RetryForever(3000);
	@Getter
	@Setter(AccessLevel.MODULE)
	private static CuratorFrameworkCache cache = new CuratorFrameworkCache();

	public CuratorFramework getCuratorFramework(String connection) {
		return cacheMap.computeIfAbsent(connection, k -> {
			if (connection.contains("/")) {
				String[] serverPath = connection.split("/", 2);
				CuratorFramework basePathFramework = getCuratorFramework(serverPath[0]);
				try {
					Stat stat = basePathFramework.checkExists().creatingParentsIfNeeded().forPath("/" + serverPath[1]);
					if (stat == null) {
						String value = basePathFramework.create().forPath("/" + serverPath[1], new byte[0]);
						log.debug("{}, {}", stat, value);
					}
				} catch (Exception e) {
					log.error("Unable to create base path /" + serverPath[1]);
				}
			}
			CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(connection, retryPolicy);
			curatorFramework.start();
			try {
				curatorFramework.blockUntilConnected();
			} catch (InterruptedException e) {
				// do nothing
			}
			return curatorFramework;
		});
	}

}
