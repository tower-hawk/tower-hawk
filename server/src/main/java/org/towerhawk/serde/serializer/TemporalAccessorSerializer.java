package org.towerhawk.serde.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.towerhawk.spring.config.Configuration;

import java.io.IOException;
import java.time.temporal.TemporalAccessor;

public class TemporalAccessorSerializer extends StdSerializer<TemporalAccessor> {

	private Configuration configuration;

	public TemporalAccessorSerializer(Configuration configuration) {
		super(TemporalAccessor.class);
		this.configuration = configuration;
	}

	@Override
	public void serialize(TemporalAccessor value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		String formattedZonedDateTime = configuration.getDateTimeFormatter().format(value);
		gen.writeString(formattedZonedDateTime);
	}
}
