package org.towerhawk.monitor.active;

public class Disabled implements ActiveCheck {

	@Override
	public boolean isActive() {
		return false;
	}
}
