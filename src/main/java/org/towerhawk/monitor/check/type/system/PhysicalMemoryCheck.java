package org.towerhawk.monitor.check.type.system;

import com.sun.management.OperatingSystemMXBean;
import org.towerhawk.monitor.check.CheckContext;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.threshold.SimpleNumericThreshold;
import org.towerhawk.serde.resolver.CheckType;

import java.lang.management.ManagementFactory;

@CheckType("physicalMemory")
public class PhysicalMemoryCheck extends AbstractCheck {

	public PhysicalMemoryCheck() {
		setCacheMs(0);
		setThreshold(SimpleNumericThreshold.builder().warnUpper(90).critUpper(95).build());
	}

	@Override
	protected void doRun(CheckRun.Builder builder, CheckContext checkContext) throws InterruptedException {
		builder.succeeded();
		if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
			OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			double freePhysicalMemory = os.getFreePhysicalMemorySize();
			double totalPhysicalMemory = os.getTotalPhysicalMemorySize();
			double percentUsed = (1 - (freePhysicalMemory / totalPhysicalMemory)) * 100;
			getThreshold().evaluate(builder, percentUsed);
			builder.addContext("freePhysicalMemory", freePhysicalMemory)
				.addContext("totalPhysicalMemory", totalPhysicalMemory)
				.addContext("percentMemoryUsed", percentUsed);
		} else {
			builder.addContext("swapSpace", "Cannot get memory usage information from jvm");
		}
	}
}
