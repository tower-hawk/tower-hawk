package org.towerhawk.monitor.check.type.system;

import com.sun.management.OperatingSystemMXBean;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.threshold.SimpleNumericThreshold;
import org.towerhawk.serde.resolver.CheckType;

import java.lang.management.ManagementFactory;

@CheckType("physicalMemory")
public class PhysicalMemoryCheck extends AbstractCheck {

	public PhysicalMemoryCheck() {
		cacheMs = 0;
		threshold = SimpleNumericThreshold.builder().warnUpper(0.9).critUpper(0.95).build();
	}

	@Override
	protected void doRun(CheckRun.Builder builder) throws InterruptedException {
		builder.succeeded();
		if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
			OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			double freePhysicalMemory = os.getFreePhysicalMemorySize();
			double totalPhysicalMemory = os.getTotalPhysicalMemorySize();
			getThreshold().evaluate(builder, freePhysicalMemory / totalPhysicalMemory);
			builder.addContext("freePhysicalMemory", freePhysicalMemory)
				.addContext("totalPhysicalMemory", totalPhysicalMemory);
		} else {
			builder.addContext("swapSpace", "Cannot get swap information from jvm");
		}
	}
}
