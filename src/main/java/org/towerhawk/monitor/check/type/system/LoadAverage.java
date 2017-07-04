package org.towerhawk.monitor.check.type.system;

import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;

import java.lang.management.ManagementFactory;

public class LoadAverage extends AbstractCheck{

	private int availableProcs = ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors();
	private double ratioWarning = 2;
	private double ratioCritical = 4;

	@Override
	protected void doRun(CheckRun.Builder builder) throws InterruptedException {
		builder.succeeded();
		java.lang.management.OperatingSystemMXBean os =  ManagementFactory.getOperatingSystemMXBean();
		double loadAverage = os.getSystemLoadAverage();
		double loadAverageRatio = loadAverage / availableProcs;
		if (loadAverageRatio > ratioCritical) {
			builder.critical();
			builder.message(String.valueOf(loadAverage) + " > " + String.valueOf(availableProcs * ratioCritical));
		} else if (loadAverageRatio > ratioWarning) {
			builder.warning();
			builder.message(String.valueOf(loadAverage) + " > " + String.valueOf(availableProcs * ratioWarning));
		}
		builder.addContext("loadAverage", loadAverage).addContext("availableProcessors", availableProcs);
	}

	public double getRatioWarning() {
		return ratioWarning;
	}

	public void setRatioWarning(double ratioWarning) {
		this.ratioWarning = ratioWarning;
	}

	public double getRatioCritical() {
		return ratioCritical;
	}

	public void setRatioCritical(double ratioCritical) {
		this.ratioCritical = ratioCritical;
	}
}
