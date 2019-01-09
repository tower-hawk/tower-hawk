package org.towerhawk.config;

import lombok.extern.slf4j.Slf4j;
import org.towerhawk.config.AbstractConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class AbstractReflectiveConfig extends AbstractConfig {

	private Map<String, Method> fieldMap = new HashMap<>();

	@Override
	protected Object get(String key) {
		try {
			return fieldMap.computeIfAbsent(key, this::resolveGetMethod).invoke(this);
		} catch (Exception e) {
			StackTraceElement element = getFirstNonClassElement();
			log.error("Tried to get {} at {}#{}:{} but value was not found!", key, element.getClassName(), element.getMethodName(), element.getLineNumber());
		}
		return null;
	}

	private Method resolveGetMethod(String key) {
		String lowerKey = key.toLowerCase();
		for (Method method : this.getClass().getMethods()) {
			//matches is or get prefixes with an exact ending match
			int index = method.getName().toLowerCase().indexOf(lowerKey);
			if (index > -1) {
				String start = method.getName().substring(0, index);
				//prevents matching only part of the string
				if (("is".equals(start) || "get".equals(start)) && method.getName().substring(index).toLowerCase().equals(lowerKey)) {
					return method;
				}
			}
		}
		return null;
	}
}
