package org.towerhawk.serde.resolver;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractTypeResolver extends TypeIdResolverBase {

	private static Map<Class<? extends Annotation>, Map<String, JavaType>> types = new HashMap<>();

	private Map<String, JavaType> getTypes() {
		return types.get(getAnnotationType());
	}

	abstract protected Class<? extends Annotation> getAnnotationType();

	abstract protected String getType(Class c);

	@Override
	public void init(JavaType bt) {
		super.init(bt);
		if (getTypes() == null) {
			types.put(getAnnotationType(), new HashMap<>());
		} else {
			// We've already initialized for this class and jackson is creating a new type resolver
			return;
		}
		ClassPathScanningCandidateComponentProvider scanner =
			new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(getAnnotationType()));

		for (BeanDefinition bd : scanner.findCandidateComponents("org.towerhawk")) {
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				String type = getType(clazz);
				if (getTypes().get(type) == null) {
					log.info("Mapping type {}.{} to class {}", getAnnotationType().getSimpleName(), type, clazz.getCanonicalName());
					JavaType javaType = TypeFactory.defaultInstance().constructSimpleType(clazz, null);
					getTypes().put(type, javaType);
				}
			} catch (Exception e) {
				log.error("Unable to get type information for {}", bd.getBeanClassName(), e);
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public String idFromValue(Object obj) {
		return idFromValueAndType(obj, obj.getClass());
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> suggestedType) {
		return null;
	}

	@Override
	public JsonTypeInfo.Id getMechanism() {
		return JsonTypeInfo.Id.CUSTOM;
	}

	@Override
	public JavaType typeFromId(DatabindContext context, String id) throws IOException {
		JavaType type = getTypes().get(id);
		if (type == null) {
			log.error("Unable to find class that matches type {}. Is the type correct?", id);
		}
		return type;
	}
}
