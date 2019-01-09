package org.towerhawk.monitor.descriptors;

public interface Prioritizable {

	/**
	 * Checks with higher priorities must be run first. This method can also be called to
	 * run checks with a certain priority.
	 *
	 * @return the priority of the check
	 */
	default byte getPriority() {
		return 0;
	}
}
