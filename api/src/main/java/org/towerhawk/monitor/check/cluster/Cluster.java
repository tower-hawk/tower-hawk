package org.towerhawk.monitor.check.cluster;

import org.pf4j.ExtensionPoint;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;

public interface Cluster extends ExtensionPoint, AutoCloseable {

	enum Status {
		MASTER,
		WORKER,
		DEAD,
		UNKNOWN
	}

	boolean isLeader();

	default boolean canExecute() {
		return isLeader();
	}

	void init(Cluster previous, Check check, Config config) throws Exception;
}
