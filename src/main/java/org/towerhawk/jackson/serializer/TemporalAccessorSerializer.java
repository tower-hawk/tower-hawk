package org.towerhawk.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.towerhawk.spring.Configuration;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class TemporalAccessorSerializer extends StdSerializer<TemporalAccessor> {

	private DateTimeFormatter formatter;

	public TemporalAccessorSerializer() {
		super(TemporalAccessor.class);
		formatter = Configuration.get().getDateTimeFormatter();
	}

	@Override
	public void serialize(TemporalAccessor value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		String formattedZonedDateTime = formatter.format(value);
		gen.writeString(formattedZonedDateTime);
	}
}
