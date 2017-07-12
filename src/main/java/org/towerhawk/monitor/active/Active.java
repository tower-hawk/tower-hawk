package org.towerhawk.monitor.active;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.towerhawk.serde.resolver.ActiveTypeResolver;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true, defaultImpl = DailySchedule.class)
@JsonTypeIdResolver(ActiveTypeResolver.class)
public interface Active {

	boolean isActive();
}
