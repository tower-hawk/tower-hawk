package org.towerhawk.config;

public interface Config {

	String getString(String key);

	String getString(String key, String defaultValue);

	Long getLong(String key);

	Long getLong(String key, Long defaultValue);

	Integer getInt(String key);

	Integer getInt(String key, Integer defaultValue);

	Short getShort(String key);

	Short getShort(String key, Short defaultValue);

	Byte getByte(String key);

	Byte getByte(String key, Byte defaultValue);

	Double getDouble(String key);

	Double getDouble(String key, Double defaultValue);

	Float getFloat(String key);

	Float getFloat(String key, Float defaultValue);

	<T> T get(String key, Class<T> type);

	<T> T get(String key, Class<T> type, T defaultValue);
}
