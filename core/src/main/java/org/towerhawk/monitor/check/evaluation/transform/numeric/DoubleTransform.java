package org.towerhawk.monitor.check.evaluation.transform.numeric;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.towerhawk.monitor.check.evaluation.transform.Transform;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class DoubleTransform implements Transform<Double> {

	@Override
	public Double transform(Object value) throws Exception {
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else {
			return (Double.valueOf(value.toString()));
		}
	}
}
