package org.towerhawk.monitor.check.type.system;

import com.sun.management.OperatingSystemMXBean;
import org.towerhawk.jackson.resolver.CheckType;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;

import java.lang.management.ManagementFactory;

@CheckType("physicalMemory")
public class PhysicalMemory extends AbstractCheck{
	private double percentWarning = 0.95;
	private double percentCritical = 0.99;

	@Override
	protected void doRun(CheckRun.Builder builder) throws InterruptedException {
		builder.succeeded();
		if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
			OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			double freePhysicalMemory = os.getFreePhysicalMemorySize();
			double totalPhysicalMemory = os.getTotalPhysicalMemorySize();
			double physicalMemoryRatio = freePhysicalMemory / totalPhysicalMemory;
			if (physicalMemoryRatio > percentCritical) {
				builder.critical();
				builder.message(String.valueOf(physicalMemoryRatio) + " > " + String.valueOf(percentCritical));
			} else if (physicalMemoryRatio > percentWarning) {
				builder.warning();
				builder.message(String.valueOf(physicalMemoryRatio) + " > " + String.valueOf(percentWarning));
			}
			builder.addContext("freePhysicalMemory", freePhysicalMemory)
				.addContext("totalPhysicalMemory", totalPhysicalMemory);
		} else {
			builder.message("Cannot get swap information from jvm");
		}
	}

	public double getPercentWarning() {
		return percentWarning;
	}

	public void setPercentWarning(double percentWarning) {
		this.percentWarning = percentWarning;
	}

	public double getPercentCritical() {
		return percentCritical;
	}

	public void setPercentCritical(double percentCritical) {
		this.percentCritical = percentCritical;
	}
}
