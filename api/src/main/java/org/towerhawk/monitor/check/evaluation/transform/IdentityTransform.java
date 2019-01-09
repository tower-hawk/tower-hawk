package org.towerhawk.monitor.check.evaluation.transform;

import org.pf4j.Extension;
import org.towerhawk.serde.resolver.TowerhawkType;

@Extension
@TowerhawkType("identity")
public class IdentityTransform implements Transform {
	@Override
	public Object transform(Object value) throws Exception {
		return value;
	}
}
