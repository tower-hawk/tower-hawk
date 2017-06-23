package org.towerhawk.check.active;

public class AlwaysActive implements ActiveCheck {

	@Override
	public boolean isActive() {
		return true;
	}
}
