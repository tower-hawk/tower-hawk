package org.towerhawk.monitor.active;

import org.towerhawk.serde.resolver.ActiveType;

@ActiveType("enabled")
public class Enabled implements Active {

	@Override
	public boolean isActive() {
		return true;
	}
}
