package org.towerhawk.monitor.schedule;

import com.coreoz.wisp.schedule.Schedule;
import org.pf4j.ExtensionPoint;

import java.util.Map;

/**
 * An interface that contains Schedules
 */
public interface ScheduleCollector extends ExtensionPoint {

	Map<String, Schedule> getSchedules();
}
