package org.towerhawk.monitor.reader;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TowerhawkDeserializer<T> extends StdDeserializer<T> {

	protected Map<String, JavaType> classes = new HashMap<>();

	protected JavaType defaultType = null;

	protected String alternativeTypeName;

	public static final String DEFAULT_TYPE_NAME = "type";

	public TowerhawkDeserializer(Class<T> c) {
		super(c);
		String simpleName = c.getSimpleName();
		if (simpleName.length() > 0) {
			alternativeTypeName = simpleName.substring(0, 1).toLowerCase();
		}
		if (simpleName.length() > 1) {
			alternativeTypeName = alternativeTypeName + c.getSimpleName().substring(1);
		}
		alternativeTypeName = alternativeTypeName + "Type";
	}

	public void register(String s, Class c) {
		String lower = s.toLowerCase();
		if (!classes.containsKey(lower)) {
			JavaType javaType = TypeFactory.defaultInstance().constructSimpleType(c, null);
			log.info("Mapping type {}:{} to class {}", _valueClass.getSimpleName(), s, c.getCanonicalName());
			classes.put(lower, javaType);
		}
	}

	public void defaultName(String name) {
		this.defaultType = classes.get(name.toLowerCase());
	}

	@Override
	public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		ObjectMapper mapper = (ObjectMapper) jp.getCodec();
		ObjectNode root = mapper.readTree(jp);
		JsonNode typeString = root.remove(DEFAULT_TYPE_NAME);
		if (typeString == null) {
			typeString = root.remove(alternativeTypeName);
		}
		JavaType type = null;
		if (typeString != null) {
			type = classes.get(typeString.asText().toLowerCase());
		}
		type = type == null ? defaultType : type;
		if (type == null) {
			log.error("Unable to find {} for '{}'", _valueClass.getSimpleName(), root);
			return null;
		} else {
			TowerhawkType towerhawkType = type.getRawClass().getAnnotation(TowerhawkType.class);
			if (towerhawkType != null && !towerhawkType.typeField().isEmpty()) {
				root.set(towerhawkType.typeField(), typeString);
			}
		}
		try {
			T value = mapper.convertValue(root, type);
			if (log.isDebugEnabled()) {
				log.debug("Deserialized '{}' \n from '{}'", mapper.convertValue(value, String.class), root);
			}
			return value;
		} catch (RuntimeException e) {
			log.error("Unable to deserialize '{}' to type {}", root, type.getRawClass().getCanonicalName(), e);
			throw e;
		}
	}
}
