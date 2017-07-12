package org.towerhawk.monitor.check.threshold;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.serde.resolver.ThresholdTypeResolver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true, defaultImpl = SimpleNumericThreshold.class)
@JsonTypeIdResolver(ThresholdTypeResolver.class)
public interface Threshold {

	boolean isAddContext();

	boolean isSetMessage();

	CheckRun.Status evaluate(CheckRun.Builder builder, double value);

	CheckRun.Status evaluate(CheckRun.Builder builder, String value);

	CheckRun.Status evaluate(CheckRun.Builder builder, Object value);
}
