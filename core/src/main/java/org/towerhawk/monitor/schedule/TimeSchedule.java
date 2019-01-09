package org.towerhawk.monitor.schedule;

import com.coreoz.wisp.schedule.Schedule;
import com.coreoz.wisp.schedule.Schedules;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import org.pf4j.Extension;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.Collections;
import java.util.Map;

@Getter
@Extension
@TowerhawkType({"time", "daily"})
public class TimeSchedule implements ScheduleCollector {

	private String name;
	private String at;
	private Map<String, Schedule> schedules;

	@JsonCreator
	public TimeSchedule(
			@JsonProperty("at") @NonNull String at,
			@JsonProperty("name") String name
	) {
		this.at = at;
		this.name = name == null ? at : name;
		Schedule schedule = Schedules.executeAt(at);
		schedules = Collections.singletonMap(this.name, schedule);
	}

}
