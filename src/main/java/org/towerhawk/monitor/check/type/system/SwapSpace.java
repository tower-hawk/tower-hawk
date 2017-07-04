package org.towerhawk.monitor.check.type.system;

import com.sun.management.OperatingSystemMXBean;
import org.towerhawk.jackson.resolver.CheckType;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;

import java.lang.management.ManagementFactory;

@CheckType("swapSpace")
public class SwapSpace extends AbstractCheck{

	private double percentWarning = 0.7;
	private double percentCritical = 0.9;

	@Override
	protected void doRun(CheckRun.Builder builder) throws InterruptedException {
		builder.succeeded();
		if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
			OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			long freeSwapSpace = os.getFreeSwapSpaceSize();
			long totalSwapSpace = os.getTotalSwapSpaceSize();
			if (totalSwapSpace > 0) {
				double swapSpaceRatio = freeSwapSpace / totalSwapSpace;
				if (swapSpaceRatio > percentCritical) {
					builder.critical();
					builder.message(String.valueOf(swapSpaceRatio) + " > " + String.valueOf(percentCritical));
				} else if (swapSpaceRatio > percentWarning) {
					builder.warning();
					builder.message(String.valueOf(swapSpaceRatio) + " > " + String.valueOf(percentWarning));
				}
				builder.addContext("freeSwapSpace", freeSwapSpace)
					.addContext("totalSwapSpace", totalSwapSpace);
			}
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
