package org.towerhawk.monitor.schedule;

import com.coreoz.wisp.schedule.Schedule;
import lombok.Getter;
import lombok.Setter;
import org.pf4j.Extension;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Extension
@TowerhawkType("multi")
public class MultiScheduleCollector implements ScheduleCollector {

	@Setter
	private List<ScheduleCollector> schedules;

	@Override
	public Map<String, Schedule> getSchedules() {
		Map<String, Schedule> combined = new HashMap<>();
		for (ScheduleCollector sc : schedules) {
			Map<String, Schedule> childSchedules = sc.getSchedules();
			if (childSchedules != null) {
				combined.putAll(childSchedules);
			}
		}
		return combined;
	}
}
