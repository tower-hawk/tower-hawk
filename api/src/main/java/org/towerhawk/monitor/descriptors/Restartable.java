package org.towerhawk.monitor.descriptors;

import org.towerhawk.monitor.check.run.Status;

public interface Restartable {

	/**
	 * @return If this check has been set to restarting. If it is restarting, then it will
	 * not return anything other than ${@link Status#SUCCEEDED} until it has actually
	 * completed successfully
	 */
	boolean isRestarting();

	/**
	 * @param restarting true if this check is restarting, false otherwise
	 */
	void setRestarting(boolean restarting);
}
