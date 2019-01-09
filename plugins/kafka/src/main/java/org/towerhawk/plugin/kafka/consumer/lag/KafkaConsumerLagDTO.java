package org.towerhawk.plugin.kafka.consumer.lag;

import lombok.ToString;
import org.towerhawk.monitor.descriptors.Keyable;
import org.towerhawk.monitor.descriptors.Partitionable;
import org.towerhawk.monitor.descriptors.Timestampable;

//Not using lombok @Getter and @Setter because Scala needs this class and it feels
//unnecessary to add a different module to get this class to compile first.
@ToString
public class KafkaConsumerLagDTO implements Partitionable, Keyable, Timestampable {

	public static final String UNKNOWN_STRING = "-";
	public static final int UNKNOWN_NUMBER = -1;

	private String group = UNKNOWN_STRING;
	private String topic = UNKNOWN_STRING;
	private String partition = "-1";
	private long lag = UNKNOWN_NUMBER;
	private long logEndOffset = UNKNOWN_NUMBER;
	private long offset = UNKNOWN_NUMBER;
	private String clientId = UNKNOWN_STRING;
	private String consumerId = UNKNOWN_STRING;
	private String host = UNKNOWN_STRING;
	private int coordinatorId = UNKNOWN_NUMBER;
	private String coordinatorHost = UNKNOWN_STRING;
	private long timestamp = System.currentTimeMillis();

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getPartition() {
		return partition;
	}

	public void setPartition(String partition) {
		this.partition = partition;
	}

	public long getLag() {
		return lag;
	}

	public void setLag(long lag) {
		this.lag = lag;
	}

	public long getLogEndOffset() {
		return logEndOffset;
	}

	public void setLogEndOffset(long logEndOffset) {
		this.logEndOffset = logEndOffset;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getCoordinatorId() {
		return coordinatorId;
	}

	public void setCoordinatorId(int coordinatorId) {
		this.coordinatorId = coordinatorId;
	}

	public String getCoordinatorHost() {
		return coordinatorHost;
	}

	public void setCoordinatorHost(String coordinatorHost) {
		this.coordinatorHost = coordinatorHost;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String getKey() {
		return getGroup() + getTopic() + getPartition();
	}

	@Override
	public Integer getPartition(int partitions) {
		return getPartitionKey().hashCode() % partitions;
	}

	@Override
	public String getPartitionKey() {
		return getGroup();
	}
}
