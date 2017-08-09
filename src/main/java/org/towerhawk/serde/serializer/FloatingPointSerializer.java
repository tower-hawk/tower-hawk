package org.towerhawk.serde.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.spring.config.Configuration;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

@Slf4j
public class FloatingPointSerializer extends StdSerializer<Number> {

	DecimalFormat format;

	public FloatingPointSerializer(Configuration configuration) {
		super(Number.class);
		StringBuilder sb = new StringBuilder("#.");
		for (int i = 0; i < configuration.getFloatingPointSerializationPrecision(); i++) {
			sb.append("#");
		}
		String stringFormat = sb.toString();
		format = new DecimalFormat(stringFormat);
		format.setRoundingMode(RoundingMode.HALF_UP);
		log.info("Created double formatter with format '{}'", stringFormat);
	}

	@Override
	public void serialize(Number value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeNumber(format.format(value.doubleValue()));
	}
}
