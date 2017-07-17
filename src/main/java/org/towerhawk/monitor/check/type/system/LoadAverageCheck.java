package org.towerhawk.monitor.check.type.system;

import lombok.Getter;
import lombok.Setter;
import org.towerhawk.monitor.check.CheckContext;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.threshold.SimpleNumericThreshold;
import org.towerhawk.serde.resolver.CheckType;

import java.lang.management.ManagementFactory;

@CheckType("loadAverage")
public class LoadAverageCheck extends AbstractCheck {

	private int availableProcs = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	@Getter
	@Setter
	boolean loadRatio = true;

	public LoadAverageCheck() {
		setCacheMs(0);
		setThreshold(SimpleNumericThreshold.builder().warnUpper(2).critUpper(4).build());
	}

	@Override
	protected void doRun(CheckRun.Builder builder, CheckContext checkContext) throws InterruptedException {
		builder.succeeded();
		java.lang.management.OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		double loadAverage = os.getSystemLoadAverage();
		double loadAverageCalc = loadAverage;
		if (loadRatio) {
			loadAverageCalc = loadAverage / availableProcs;
		}
		getThreshold().evaluate(builder, loadAverageCalc);
		builder.addContext("loadAverage", loadAverage)
			.addContext("availableProcessors", availableProcs)
			.addContext("useLoadRatio", loadRatio);
	}
}