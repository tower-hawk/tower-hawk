package org.towerhawk.plugin.kafka.consumer.lag;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.execution.ExecutionResult;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Extension
@TowerhawkType("multiKafkaConsumerLag")
public class MultiKafkaConsumerLagCheck implements CheckExecutor {

	protected String brokers = "localhost:9092";
	protected List<String> groups;
	protected boolean throwErrorIfNotExists = false;
	@Setter(AccessLevel.NONE)
	protected List<KafkaConsumerLagCheck> checks;

	@Override
	public void init(CheckExecutor checkExecutor, Check check, Config config) throws Exception {
		checks = new ArrayList<>(groups.size());
		for (String g : groups) {
			KafkaConsumerLagCheck lagCheck = new KafkaConsumerLagCheck();
			lagCheck.setBrokers(brokers);
			lagCheck.setGroup(g);
			lagCheck.setThrowErrorIfNotExists(throwErrorIfNotExists);
			lagCheck.init(checkExecutor, check, config);
			checks.add(lagCheck);
		}
	}

	@Override
	public ExecutionResult execute(CheckRun.Builder builder, RunContext context) throws Exception {
		ExecutionResult result = ExecutionResult.startTimer();
		checks.forEach(c -> {
			try {
				result.addResult(c.group, c.execute(builder, context));
			} catch (Exception e) {
				result.addResult(c.group + "-exception", e.getMessage());
			}
		});
		result.complete();
		return result;
	}

	@Override
	public void close() throws Exception {
		for (KafkaConsumerLagCheck c : checks) {
			c.close();
		}
	}
}
