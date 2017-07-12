package org.towerhawk.monitor.active;

import org.towerhawk.serde.resolver.ActiveType;

@ActiveType("disabled")
public class Disabled implements Active {

	@Override
	public boolean isActive() {
		return false;
	}
}
