package org.towerhawk.spring.autoConfig;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.towerhawk.serde.serializer.DurationSerializer;
import org.towerhawk.serde.serializer.TemporalAccessorSerializer;
import org.towerhawk.spring.config.Configuration;

import java.time.Duration;
import java.time.temporal.TemporalAccessor;

@org.springframework.context.annotation.Configuration
public class JacksonObjectMapperConfiguration {

	@Bean
	public Jackson2ObjectMapperBuilder objectMapperBuilder(Configuration configuration) {
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.serializationInclusion(JsonInclude.Include.NON_NULL);
		builder.serializerByType(Duration.class, new DurationSerializer(configuration));
		builder.serializerByType(TemporalAccessor.class, new TemporalAccessorSerializer(configuration));
		builder.indentOutput(configuration.isPrettyPrintResultJson());

		return builder;
	}
}
