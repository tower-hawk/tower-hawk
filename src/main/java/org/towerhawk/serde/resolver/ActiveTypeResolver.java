package org.towerhawk.serde.resolver;

import java.lang.annotation.Annotation;

public class ActiveTypeResolver extends AbstractTypeResolver {

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return ActiveType.class;
	}

	@Override
	protected String getType(Class c) {
		ActiveType type = (ActiveType) c.getAnnotation(ActiveType.class);
		return type.value();
	}
}
