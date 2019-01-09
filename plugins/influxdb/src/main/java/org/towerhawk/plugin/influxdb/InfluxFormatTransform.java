package org.towerhawk.plugin.influxdb;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.monitor.check.evaluation.transform.Transform;
import org.towerhawk.serde.resolver.TowerhawkType;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
@Slf4j
@Extension
@TowerhawkType("influxFormat")
public class InfluxFormatTransform implements Transform<Object> {

	private static final String SEPARATOR = ",";
	private static final String SPACE = " ";
	private static final String EQUAL = "=";

	@Setter(AccessLevel.NONE)
	@Getter(AccessLevel.NONE)
	private ObjectMapper mapper = new ObjectMapper();
	@Setter(AccessLevel.NONE)
	private JavaType type;
	private String dynamicMetricName;
	private String metricName;
	private String timestamp = "timestamp";
	private Long timestampMultiplier = 1_000_000L;
	private List<String> tags;
	private String unknownTag = "unknown";
	private List<String> fields;
	private Double unknownField = 0D;
	@Setter(AccessLevel.NONE)
	private boolean initialized = false;


	public InfluxFormatTransform() {
		JavaType key = TypeFactory.defaultInstance().constructSimpleType(String.class, null);
		JavaType value = TypeFactory.defaultInstance().constructSimpleType(Object.class, null);
		type = TypeFactory.defaultInstance().constructSimpleType(Map.class, new JavaType[]{key, value});
	}

	@PostConstruct
	public void init() {
		if (!initialized) {
			if (metricName == null && dynamicMetricName == null) {
				throw new IllegalArgumentException("Either metricName or dynamicMetricName must be set!");
			}
			if (tags == null) {
				tags = Collections.emptyList();
			}
			if (fields == null) {
				fields = Collections.emptyList();
			}
			// Sort per influx documentation for performance
			Collections.sort(tags);
			initialized = true;
		}
	}

	@Override
	public Object transform(Object value) throws Exception {
		//TODO remove this once @PostConstruct is supported
		init();
		Map<String, Object> map = mapper.convertValue(value, type);
		String name = metricName;
		if (name == null) {
			name = map.get(dynamicMetricName).toString();
		}
		if (name == null) {
			throw new IllegalArgumentException("Could not field for " + dynamicMetricName);
		}
		Object timestamp = map.get(this.timestamp);
		if (timestamp instanceof Long) {
			timestamp = (Long) timestamp * timestampMultiplier;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		tags.forEach(t -> {
			sb.append(SEPARATOR);
			sb.append(t);
			sb.append(EQUAL);
			sb.append(map.getOrDefault(t, unknownTag));
		});
		sb.append(SPACE);
		AtomicBoolean appendSeparator = new AtomicBoolean(false);
		fields.forEach(f -> {
			Object val = map.getOrDefault(f, unknownField);
			if (val instanceof Number) {
				if (appendSeparator.get()) {
					sb.append(SEPARATOR);
				} else {
					appendSeparator.set(true);
				}
				sb.append(f);
				sb.append(EQUAL);
				sb.append(val);
			}
		});
		if (timestamp != null) {
			sb.append(SPACE);
			sb.append(timestamp.toString());
		}
		//name,tag=value,tag=value metric=value,metric=value timestamp
		return sb.toString();
	}

}
