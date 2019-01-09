package org.towerhawk.monitor.check.evaluation.transform;

import org.pf4j.Extension;
import org.towerhawk.serde.resolver.TowerhawkType;

@Extension
@TowerhawkType("string")
public class StringTransform implements Transform<String> {

	@Override
	public String transform(Object value) throws Exception {
		return value.toString();
	}
}
