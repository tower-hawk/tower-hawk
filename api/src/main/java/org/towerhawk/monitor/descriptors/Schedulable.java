package org.towerhawk.monitor.descriptors;

import org.towerhawk.monitor.schedule.ScheduleCollector;

public interface Schedulable {

	ScheduleCollector getScheduleCollector();
}
