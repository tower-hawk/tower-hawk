package org.towerhawk.plugin.kafka.producer;

import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.HashMap;
import java.util.Map;

public class ProducerPropsEnhancer {

	public static Map<String, Object> supplementProps(Map<String, Object> props) {
		if (props == null) {
			props = new HashMap<>();
		}
		props.putIfAbsent(ProducerConfig.LINGER_MS_CONFIG, 100);
		props.putIfAbsent(ProducerConfig.ACKS_CONFIG, "all");
		props.putIfAbsent(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
		props.putIfAbsent(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		props.putIfAbsent(ProducerConfig.LINGER_MS_CONFIG, 1000);
		props.putIfAbsent(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
		props.putIfAbsent(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 100);
		props.putIfAbsent(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
		props.putIfAbsent(ProducerConfig.RETRIES_CONFIG, 100);
		return props;
	}

	public static void ensureSerializers(Map<String, Object> props) {
		if (!props.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)) {
			throw new IllegalArgumentException("Producer properties must have " + ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG + " set");
		}
		if (!props.containsKey(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)) {
			throw new IllegalArgumentException("Producer properties must have " + ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG + " set");
		}
	}
}
