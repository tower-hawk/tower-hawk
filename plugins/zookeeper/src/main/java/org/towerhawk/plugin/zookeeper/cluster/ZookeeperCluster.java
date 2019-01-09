package org.towerhawk.plugin.zookeeper.cluster;

import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.framework.state.ConnectionState;
import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.cluster.Cluster;
import org.towerhawk.plugin.zookeeper.CuratorFrameworkCache;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Slf4j
@Extension
@TowerhawkType("zookeeper")
public class ZookeeperCluster extends LeaderSelectorListenerAdapter implements Cluster {

	@Setter
	protected String name;
	@Setter
	protected String path;
	@Setter
	protected String zkConnect;
	@Setter
	protected long maxLeadershipTimeMs = Long.MAX_VALUE;
	@Setter
	protected boolean enableExecuteOnConnectionFailure = false;
	protected LeaderSelector leaderSelector;
	protected Check check;
	protected boolean initialized = false;
	protected volatile boolean closed = false;
	// needs to initally be false since stateChanged() is only called when a connection state changes.
	// It should be assumed that the curatorFramework is connected before any processing starts.
	protected volatile boolean shortCircuit = false;

	@Override
	public boolean isLeader() {
		return !closed && (leaderSelector.hasLeadership() || shortCircuit);
	}

	public String getLeader() {
		try {
			return leaderSelector.getLeader().getId();
		} catch (Exception e) {
			return "unknown";
		}
	}

	public List<String> getNodes() {
		try {
			return leaderSelector.getParticipants().stream().map(Participant::getId).collect(Collectors.toList());
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@Override
	@Synchronized
	public void init(Cluster previous, Check check, Config config) throws Exception {
		if (!initialized) {
			this.check = check;
			path = (path == null ? "" : path) + "/" + check.getApp().getId() + "/" + check.getId();
			if (name == null) {
				name = check.getFullName();
			}
			if (zkConnect == null) {
				zkConnect = "localhost:2181";
			}
			CuratorFramework curatorFramework = CuratorFrameworkCache.getCache().getCuratorFramework(zkConnect);
			leaderSelector = new LeaderSelector(curatorFramework, path, this);
			leaderSelector.autoRequeue();
			leaderSelector.setId(name);
			leaderSelector.start();
			initialized = true;
			closed = false;
		}
	}

	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {
		if (enableExecuteOnConnectionFailure) {
			shortCircuit = !newState.isConnected();
		}
		super.stateChanged(client, newState);
	}

	@Override
	public void close() throws Exception {
		if (!closed) {
			closed = true;
			if (leaderSelector != null) {
				leaderSelector.close();
			}
			if (check != null) {
				check.close();
			}
			initialized = false;
		}
	}

	@Override
	public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
		try {
			log.debug("Leader for {} on path {}", getName(), getPath());
			Thread.sleep(maxLeadershipTimeMs);
		} finally {
			log.debug("Relinquishing leadership for {} on path {}", getName(), getPath());
		}
	}
}
