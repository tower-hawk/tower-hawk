package org.towerhawk.monitor.descriptors;

import org.towerhawk.monitor.active.Active;

public interface Activatable {

	/**
	 *
	 * @return the underlying Active object
	 */
	Active getActive();

	/**
	 * This determines whether the check is active right now or not. This allows different
	 * strategies to be implemented like daily or weekly schedules. This can also be used
	 * to effectively disable a check.
	 *
	 * @return true if the check can run, false otherwise.
	 */
	default boolean isActive() {
		return getActive().isActive();
	}

	/**
	 * Sets the underlying active object in the case that it needs to be changed
	 * @param active
	 */
	void setActive(Active active);
}
