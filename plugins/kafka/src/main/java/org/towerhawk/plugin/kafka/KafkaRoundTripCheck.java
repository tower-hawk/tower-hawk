package org.towerhawk.plugin.kafka;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Slf4j
@Extension
@TowerhawkType("kafkaRoundTrip")
public class KafkaRoundTripCheck implements CheckExecutor, Runnable {

	protected Consumer<byte[], byte[]> consumer;
	protected Producer<byte[], byte[]> producer;

	@Setter
	protected String brokers;

	public KafkaRoundTripCheck() {
		{
			if (brokers == null) {
				brokers = System.getenv("KAFKA_BROKERS");
			}
			if (brokers == null || brokers.isEmpty()) {
				brokers = System.getProperty("KAFKA_BROKERS");
			}
			if (brokers == null || brokers.isEmpty()) {
				brokers = "localhost:9092";
			}
		}
		log.info("kafkaServers = {}", brokers);
		Map<String, Object> consumerConfigs = new HashMap<>();
		consumerConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		consumerConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		consumerConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
		consumerConfigs.put(ConsumerConfig.CLIENT_ID_CONFIG, "telegraf-to-druid-transformer");
		consumerConfigs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		consumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, "telegraf-to-druid-transfomer");
		consumerConfigs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50000);
		consumerConfigs.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 60000);
		consumerConfigs.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 70000);
		consumerConfigs.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 60000);
		consumerConfigs.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 5000);
		consumer = new KafkaConsumer<>(consumerConfigs);
		consumer.subscribe(Pattern.compile(".*"));

		Map<String, Object> producerConfigs = new HashMap<>();
		producerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
		producerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
		producerConfigs.put(ProducerConfig.ACKS_CONFIG, "all");
		producerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
		producerConfigs.put(ProducerConfig.CLIENT_ID_CONFIG, "telegraf-to-druid-transformer");
		producerConfigs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
		producerConfigs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
		producerConfigs.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
		producerConfigs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
		producerConfigs.put(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 100);
		producerConfigs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
		producerConfigs.put(ProducerConfig.RETRIES_CONFIG, 10);
		producer = new KafkaProducer<>(producerConfigs);

		Executors.newSingleThreadExecutor().submit(this);
	}

	public void init(CheckExecutor checkExecutor, Check check, Config config) throws Exception {
		consumer.poll(1000);
	}

	public ExecutionResult execute(CheckRun.Builder builder, RunContext context) throws Exception {
		return ExecutionResult.of("plugin-test");
	}

	public void close() throws Exception {
		consumer.close();
	}

	@Override
	public void run() {
		log.info("Running kafkaRoundTrip");
	}
}
