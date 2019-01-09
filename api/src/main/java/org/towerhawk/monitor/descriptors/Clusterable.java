package org.towerhawk.monitor.descriptors;

import org.towerhawk.monitor.check.cluster.Cluster;

public interface Clusterable {

	Cluster getCluster();

	void setCluster(Cluster cluster);

	default boolean canExecuteForCluster() {
		return getCluster().canExecute();
	}
}
