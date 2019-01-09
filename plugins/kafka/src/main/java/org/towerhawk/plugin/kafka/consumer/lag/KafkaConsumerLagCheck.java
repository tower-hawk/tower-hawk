package org.towerhawk.plugin.kafka.consumer.lag;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.KafkaException;
import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.plugin.PluginContext;
import org.towerhawk.serde.resolver.TowerhawkType;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@Extension
@TowerhawkType("kafkaConsumerLag")
public class KafkaConsumerLagCheck implements CheckExecutor {

	protected ConsumerLagAdapter adapter;
	protected String brokers = "localhost:9092";
	protected String group;
	protected long timeoutMs = 5000;
	protected Set<String> whitelistTopics = Collections.emptySet();
	protected Set<String> blacklistTopics = Collections.emptySet();
	protected boolean throwErrorIfNotExists = false;
	protected volatile boolean initialized = false;
	@Setter(AccessLevel.NONE)
	protected Predicate<Object> predicate;

	@Override
	@PostConstruct
	public void init(CheckExecutor checkExecutor, Check check, Config config) throws Exception {
		if (group == null) {
			throw new IllegalArgumentException("group must be set!");
		}
		Predicate<KafkaConsumerLagDTO> whitelist = whitelistTopics.isEmpty() ? l -> true : l -> whitelistTopics.contains(l.getTopic());
		Predicate<KafkaConsumerLagDTO> blacklist = blacklistTopics.isEmpty() ? l -> true : l -> !blacklistTopics.contains(l.getTopic());
		predicate = o -> o instanceof KafkaConsumerLagDTO && whitelist.test((KafkaConsumerLagDTO) o) && blacklist.test((KafkaConsumerLagDTO) o);
		//do not connect so we don't have a bunch of connections waiting when this isn't the leader
	}

	protected void initialize() {
		if (!initialized) {
			ClassLoader cached = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(PluginContext.getClassLoader(this.getClass()));
			adapter = new ConsumerLagAdapter(brokers, group, String.valueOf(timeoutMs), throwErrorIfNotExists);
			adapter.getLags();
			Thread.currentThread().setContextClassLoader(cached);
			initialized = true;
		}
	}

	@Override
	public ExecutionResult execute(CheckRun.Builder builder, RunContext context) throws Exception {
		try {
			return getResult();
		} catch (KafkaException e) {
			log.warn("Caught KafkaException while trying to get lags. Retrying...", e);
			close();
			return getResult();
		}
	}

	protected ExecutionResult getResult() {
		initialize();
		ExecutionResult result = adapter.getLags();
		Object o = result.get(ExecutionResult.RESULT);
		if (o instanceof List) {
			List<?> lags = (List<?>)o;
			lags = lags.stream().filter(predicate).collect(Collectors.toList());
			result.setResult(lags);
		}
		return result;

	}

	@Override
	public void close() throws Exception {
		if (adapter != null) {
			adapter.close();
		}
		initialized = false;
	}
}
