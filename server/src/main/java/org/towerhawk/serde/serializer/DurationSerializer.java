package org.towerhawk.serde.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.towerhawk.spring.config.Configuration;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;

public class DurationSerializer extends StdSerializer<Duration> {

	private Configuration configuration;

	public DurationSerializer(Configuration configuration) {
		super(Duration.class);
		this.configuration = configuration;
	}

	@Override
	public void serialize(Duration value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		long nanos = value.toNanos();
		//TimeUnit should have better support for exact conversions
		double convertedDuration;
		switch (configuration.getDurationTimeUnit()) {
			case NANOSECONDS:
				convertedDuration = nanos;
				break;
			case MICROSECONDS:
				convertedDuration = nanos / 1_000D;
				break;
			case MILLISECONDS:
				convertedDuration = nanos / 1_000_000D;
				break;
			default:
				convertedDuration = nanos / 1_000_000_000D;
		}
		BigDecimal bigDecimalDuration = BigDecimal.valueOf(convertedDuration);
		gen.writeNumber(bigDecimalDuration);
	}
}
