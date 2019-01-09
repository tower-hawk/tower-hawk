package org.towerhawk.monitor.schedule;

import com.coreoz.wisp.schedule.Schedule;
import com.coreoz.wisp.schedule.Schedules;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import org.pf4j.Extension;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

/**
 * An implementation of schedule that is based solely on milliseconds.
 */
@Getter
@Extension
@TowerhawkType({"default", "fixed", "fixedDelay", "duration"})
public class FixedDelaySchedule implements ScheduleCollector {

	private String name;
	private Duration duration;
	private Map<String, Schedule> schedules;

	@JsonCreator
	public FixedDelaySchedule(
			@JsonProperty("duration") @NonNull String durationString,
			@JsonProperty("name") String name
	) {
		this.duration = Duration.parse(durationString);
		this.name = name == null ? durationString : name;
		Schedule schedule = Schedules.fixedDelaySchedule(duration);
		schedules = Collections.singletonMap(this.name, schedule);
	}
}