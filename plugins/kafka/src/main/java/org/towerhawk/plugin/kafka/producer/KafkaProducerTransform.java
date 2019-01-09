package org.towerhawk.plugin.kafka.producer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.pf4j.Extension;
import org.towerhawk.monitor.check.evaluation.transform.IdentityTransform;
import org.towerhawk.monitor.check.evaluation.transform.Transform;
import org.towerhawk.monitor.descriptors.Keyable;
import org.towerhawk.monitor.descriptors.Partitionable;
import org.towerhawk.monitor.descriptors.Routable;
import org.towerhawk.monitor.descriptors.Timestampable;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Slf4j
@Extension
@TowerhawkType("kafkaProducer")
public class KafkaProducerTransform implements Transform {

	@Setter(AccessLevel.NONE)
	@JsonIgnore
	private ObjectMapper mapper = new ObjectMapper();
	@Setter(AccessLevel.NONE)
	private Producer<String, String> producer;
	private String topic;
	private String brokerList;
	private boolean deepUnwrap;
	private boolean unwrap;
	private transient int partitionCount;
	private Transform preProduceTransform;

	@JsonCreator
	public KafkaProducerTransform(
			@JsonProperty("producerProperties") Map<String, Object> producerProperties,
			@JsonProperty("topic") @NonNull String topic,
			@JsonProperty("unwrap") Boolean unwrap,
			@JsonProperty("deepUnwrap") Boolean deepUnwrap,
			@JsonProperty("preProduceTransform") Transform preProduceTransform,
			@JsonProperty("brokerList") String brokerList
	) {
		this.topic = topic;
		this.brokerList = brokerList == null ? "localhost:9092" : brokerList;
		this.unwrap = unwrap == null ? true : unwrap;
		this.deepUnwrap = deepUnwrap == null ? false : deepUnwrap;
		producerProperties = ProducerPropsEnhancer.supplementProps(producerProperties);
		producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.brokerList);
		producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProperties.putIfAbsent(ProducerConfig.CLIENT_ID_CONFIG, "towerhawk-producer-transform-" + UUID.randomUUID().toString());

		this.producer = new KafkaProducer<>(producerProperties);
		partitionCount = producer.partitionsFor(topic).size();

		if (preProduceTransform == null) {
			preProduceTransform = new IdentityTransform();
		}
		this.preProduceTransform = preProduceTransform;
	}

	@Override
	public Object transform(Object value) throws Exception {
		if (unwrap && value instanceof Map) {
			for (Object entry : ((Map) value).values()) {
				send(entry);
			}
		} else if (unwrap && value instanceof Iterable) {
			Iterator i = ((Iterable) value).iterator();
			while (i.hasNext()) {
				send(i.next());
			}
		} else {
			send(value);
		}
		producer.flush();
		return value;
	}

	private void send(Object value) throws Exception {
		if (deepUnwrap && value instanceof Map) {
			for (Object entry : ((Map) value).values()) {
				send(entry);
			}
			return;
		} else if (deepUnwrap && value instanceof Iterable) {
			Iterator i = ((Iterable) value).iterator();
			while (i.hasNext()) {
				send(i.next());
			}
			return;
		}

		try {
			value = preProduceTransform.transform(value);
			String key = "";
			Integer partition = null;
			long timestamp;
			String topic = null;
			if (value instanceof Keyable) {
				key = ((Keyable) value).getKey();
			} else if (value instanceof Partitionable) {
				key = ((Partitionable) value).getPartitionKey();
				partition = ((Partitionable) value).getPartition(partitionCount);
			}
			if (value instanceof Timestampable) {
				timestamp = ((Timestampable) value).getTimestamp();
			} else {
				timestamp = System.currentTimeMillis();
			}
			if (value instanceof Routable) {
				topic = ((Routable) value).getQueueName();
			}
			if (topic == null) {
				topic = this.topic;
			}

			String val = value instanceof String ? (String) value : mapper.writeValueAsString(value);
			ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, partition, timestamp, key, val);
			producer.send(producerRecord);
		} catch (Exception e) {
			log.error("Unable to process record {}", value.toString(), e);
			throw e;
		}
	}
}
