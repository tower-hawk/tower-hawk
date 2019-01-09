package org.towerhawk.monitor.descriptors;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Routable {

	@JsonIgnore
	String getQueueName();
}
