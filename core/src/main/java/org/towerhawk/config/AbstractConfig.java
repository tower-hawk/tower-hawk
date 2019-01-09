package org.towerhawk.config;

import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public abstract class AbstractConfig implements Config {

	protected abstract Object get(String key);

	protected Object getOrDefault(String key, Object defaultValue) {
		Object value = get(key);
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

	protected Number getOrDefaultFloat(String key, Number defaultValue) {
		Object obj = getOrDefault(key, defaultValue);
		if (obj == null) {
			return null;
		}
		if (obj instanceof Number) {
			return (Number) obj;
		}
		try {
			Number n = Double.valueOf(obj.toString());
			return n;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	protected Number getOrDefaultInt(String key, Number defaultValue) {
		Object obj = getOrDefault(key, defaultValue);
		if (obj == null) {
			return null;
		}
		if (obj instanceof Number) {
			return (Number) obj;
		}
		try {
			Number n = Long.valueOf(obj.toString());
			return n;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	@Override
	public String getString(String key) {
		return getString(key, null);
	}

	@Override
	public String getString(String key, String defaultValue) {
		return getOrDefault(key, defaultValue).toString();
	}

	@Override
	public Long getLong(String key) {
		return getLong(key, null);
	}

	@Override
	public Long getLong(String key, Long defaultValue) {
		Number n = getOrDefaultInt(key, defaultValue);
		if (n != null) {
			return n.longValue();
		}
		return null;
	}

	@Override
	public Integer getInt(String key) {
		return getInt(key, null);
	}

	@Override
	public Integer getInt(String key, Integer defaultValue) {
		Number n = getOrDefaultInt(key, defaultValue);
		if (n != null) {
			return n.intValue();
		}
		return null;
	}

	@Override
	public Short getShort(String key) {
		return getShort(key, null);
	}

	@Override
	public Short getShort(String key, Short defaultValue) {
		Number n = getOrDefaultInt(key, defaultValue);
		if (n != null) {
			return n.shortValue();
		}
		return null;
	}

	@Override
	public Byte getByte(String key) {
		return getByte(key, null);
	}

	@Override
	public Byte getByte(String key, Byte defaultValue) {
		Number n = getOrDefaultInt(key, defaultValue);
		if (n != null) {
			return n.byteValue();
		}
		return null;
	}

	@Override
	public Double getDouble(String key) {
		return getDouble(key, null);
	}

	@Override
	public Double getDouble(String key, Double defaultValue) {
		Number n = getOrDefaultFloat(key, defaultValue);
		if (n != null) {
			return n.doubleValue();
		}
		return null;
	}

	@Override
	public Float getFloat(String key) {
		return getFloat(key, null);
	}

	@Override
	public Float getFloat(String key, Float defaultValue) {
		Number n = getOrDefaultFloat(key, defaultValue);
		if (n != null) {
			return n.floatValue();
		}
		return null;
	}

	@Override
	public <T> T get(String key, Class<T> type) {
		return get(key, type, null);
	}

	@Override
	public <T> T get(String key, Class<T> type, T defaultValue) {
		Object obj = getOrDefault(key, defaultValue);
		if (type.isInstance(obj)) {
			@SuppressWarnings("unchecked")
			T t = (T) obj;
			return t;
		} else {
			StackTraceElement element = getFirstNonClassElement();
			if (element != null) {
				log.warn("Returning default to {}#{} because returned value {} is not instance of {}", element.getClassName(), element.getMethodName(), obj, type.getCanonicalName());
			}
			return defaultValue;
		}
	}

	protected StackTraceElement getFirstNonClassElement() {
		Set<String> superClasses = new HashSet<>();
		superClasses.add(Thread.class.getCanonicalName());
		for (Class c = this.getClass(); !Object.class.equals(c); c = c.getSuperclass()) {
			superClasses.add(c.getCanonicalName());
		}
		for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
			if (!superClasses.contains(e.getClassName())) {
				return e;
			}
		}
		return null;
	}
}
