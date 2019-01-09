package org.towerhawk.monitor.check.run;

import java.util.List;

public interface CheckRunAccumulator {

	List<CheckRun> getChecks();

	List<CheckRun> waitForChecks() throws InterruptedException;

	void cancelChecks();

}
