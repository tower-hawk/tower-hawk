package org.towerhawk.monitor.check.type.system;

import lombok.Getter;
import lombok.Setter;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.threshold.SimpleNumericThreshold;
import org.towerhawk.monitor.check.threshold.eval.NumericThresholdEvaluator;
import org.towerhawk.serde.resolver.CheckType;
import org.towerhawk.spring.config.Configuration;

import java.lang.management.ManagementFactory;

@CheckType("loadAverage")
public class LoadAverageCheck extends AbstractCheck {

	private int availableProcs = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	@Getter
	@Setter
	boolean loadRatio = true;

	public LoadAverageCheck() {
		cacheMs = 0;
		threshold = SimpleNumericThreshold.builder().warnUpper(2).critUpper(4).build();
	}

	@Override
	protected void doRun(CheckRun.Builder builder) throws InterruptedException {
		builder.succeeded();
		java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		double loadAverage = os.getSystemLoadAverage();
		getThreshold().evaluate(builder, loadAverage);
		builder.addContext("loadAverage", loadAverage).addContext("availableProcessors", availableProcs);
	}
}