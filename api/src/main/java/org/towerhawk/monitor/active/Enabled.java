package org.towerhawk.monitor.active;

import org.pf4j.Extension;
import org.towerhawk.serde.resolver.TowerhawkType;

@Extension
@TowerhawkType(value = {"enabled", "default"})
public class Enabled implements Active {

	@Override
	public boolean isActive() {
		return true;
	}
}
