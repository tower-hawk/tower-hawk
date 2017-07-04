package org.towerhawk.jackson.resolver;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CheckTypeResolver extends TypeIdResolverBase {

	private Logger log = LoggerFactory.getLogger(getClass());
	private static Map<String, JavaType> types = new LinkedHashMap<>();

	@Override
	public void init(JavaType bt) {
		super.init(bt);
		ClassPathScanningCandidateComponentProvider scanner =
			new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(CheckType.class));

		for (BeanDefinition bd : scanner.findCandidateComponents("org.towerhawk")) {
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				CheckType checkType = clazz.getAnnotation(CheckType.class);
				String type = checkType.value();
				if (types.get(type) == null) {
					log.info("Mapping type {} to class {}", type, clazz.getCanonicalName());
					types.put(type, SimpleType.constructUnsafe(clazz));
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
		JavaType type = types.get(id);
		if (type == null) {
			log.error("Unable to find class that matches type {}. Is it annotated?");
		}
		return type;
	}
}
