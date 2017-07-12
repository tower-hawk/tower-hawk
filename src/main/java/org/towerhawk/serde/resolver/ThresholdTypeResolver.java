package org.towerhawk.serde.resolver;

import java.lang.annotation.Annotation;

public class ThresholdTypeResolver extends AbstractTypeResolver {

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return ThresholdType.class;
	}

	@Override
	protected String getType(Class c) {
		ThresholdType type = (ThresholdType) c.getAnnotation(ThresholdType.class);
		return type.value();
	}
}
