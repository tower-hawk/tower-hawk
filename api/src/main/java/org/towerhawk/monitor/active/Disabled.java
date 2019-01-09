package org.towerhawk.monitor.active;

import org.pf4j.Extension;
import org.towerhawk.serde.resolver.TowerhawkType;

@Extension
@TowerhawkType("disabled")
public class Disabled implements Active {

	@Override
	public boolean isActive() {
		return false;
	}
}
