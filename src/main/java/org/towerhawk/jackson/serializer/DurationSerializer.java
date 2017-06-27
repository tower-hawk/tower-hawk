package org.towerhawk.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;

public class DurationSerializer extends StdSerializer<Duration> {

	public DurationSerializer() {
		super(Duration.class);
	}

	@Override
	public void serialize(Duration value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		BigDecimal convertedDuration = BigDecimal.valueOf(value.toNanos() / 1_000_000_000D);
		gen.writeNumber(convertedDuration);
	}
}
