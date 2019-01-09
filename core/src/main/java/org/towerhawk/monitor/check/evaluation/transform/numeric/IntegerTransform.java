package org.towerhawk.monitor.check.evaluation.transform.numeric;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.towerhawk.monitor.check.evaluation.transform.Transform;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class IntegerTransform implements Transform<Integer> {

	@Override
	public Integer transform(Object value) throws Exception {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		} else {
			return (Integer.valueOf(value.toString()));
		}
	}
}
