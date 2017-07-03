package org.towerhawk.monitor.active;

public class Enabled implements ActiveCheck {

	@Override
	public boolean isActive() {
		return true;
	}
}
