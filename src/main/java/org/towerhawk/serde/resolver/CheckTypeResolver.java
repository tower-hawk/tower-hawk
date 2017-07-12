package org.towerhawk.serde.resolver;

import java.lang.annotation.Annotation;

public class CheckTypeResolver extends AbstractTypeResolver {

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return CheckType.class;
	}

	@Override
	protected String getType(Class c) {
		CheckType type = (CheckType)c.getAnnotation(CheckType.class);
		return type.value();
	}
}
