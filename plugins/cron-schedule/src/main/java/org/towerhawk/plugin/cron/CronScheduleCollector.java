package org.towerhawk.plugin.cron;

import com.coreoz.wisp.schedule.Schedule;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import org.pf4j.Extension;
import org.towerhawk.monitor.schedule.ScheduleCollector;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.Collections;
import java.util.Map;

@Getter
@Extension
@TowerhawkType(value = {"cron", "unixCron", "quartz", "quartzCron"}, typeField = "cronType")
public class CronScheduleCollector implements ScheduleCollector {

	private String cronType;
	private String name;
	private String cron;
	private Map<String, Schedule> schedules;

	@JsonCreator
	public CronScheduleCollector(
			@JsonProperty("cron") @NonNull String cron,
			@JsonProperty("name") String name,
			@JsonProperty("cronType") @NonNull String cronType
	) {
		this.cron = cron;
		this.name = name == null ? cron : name;
		this.cronType = cronType;
		Schedule schedule;
		if (cronType.contains("quartz")) {
			schedule = CronSchedule.parseQuartzCron(cron);
		} else {
			schedule = CronSchedule.parseUnixCron(cron);
		}
		schedules = Collections.singletonMap(this.name, schedule);
	}

}
