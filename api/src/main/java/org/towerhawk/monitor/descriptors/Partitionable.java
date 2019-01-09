package org.towerhawk.monitor.descriptors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Partitionable {

	/**
	 * @return a nullable ${@Integer} representing the partition this object belongs to
	 */
	Integer getPartition(int partitions);

	/**
	 * @return a non-null ${@String} that can be hashed to return a partition from a Partitioner
	 */
	@JsonIgnore
	String getPartitionKey();
}
