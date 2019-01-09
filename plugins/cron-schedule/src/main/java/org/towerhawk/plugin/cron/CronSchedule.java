package org.towerhawk.plugin.cron;

import com.coreoz.wisp.schedule.Schedule;
import com.cronutils.descriptor.CronDescriptor;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

// lifted from com.coreoz.wisp.schedule.cron.CronSchedule to get around the threeten dependency
// this class can be removed once the other class is fixed.
public class CronSchedule implements Schedule {

	private static final CronParser UNIX_CRON_PARSER = new CronParser(
			CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
	);
	private static final CronParser QUARTZ_CRON_PARSER = new CronParser(
			CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ)
	);

	private static final CronDescriptor ENGLISH_DESCRIPTOR = CronDescriptor.instance(Locale.ENGLISH);

	private final ExecutionTime cronExpression;
	private final String description;

	public CronSchedule(Cron cronExpression) {
		this.cronExpression = ExecutionTime.forCron(cronExpression);
		this.description = ENGLISH_DESCRIPTOR.describe(cronExpression);
	}

	@Override
	public long nextExecutionInMillis(long currentTimeInMillis, int executionsCount, Long lastExecutionTimeInMillis) {
		Instant currentInstant = Instant.ofEpochMilli(currentTimeInMillis);
		ZonedDateTime currentDateTime = ZonedDateTime.ofInstant(
				currentInstant,
				ZoneId.systemDefault()
		);
		return cronExpression.timeToNextExecution(currentDateTime)
				.map(d -> currentInstant.plus(d).toEpochMilli())
				.orElse(Schedule.WILL_NOT_BE_EXECUTED_AGAIN);
	}

	@Override
	public String toString() {
		return description;
	}

	/**
	 * Create a {@link Schedule} from a cron expression based on the Unix format,
	 * e.g. 1 * * * * for each minute.
	 */
	public static CronSchedule parseUnixCron(String cronExpression) {
		return new CronSchedule(UNIX_CRON_PARSER.parse(cronExpression));
	}

	/**
	 * Create a {@link Schedule} from a cron expression based on the Quartz format,
	 * e.g. 0 * * * * ? * for each minute.
	 */
	public static CronSchedule parseQuartzCron(String cronExpression) {
		return new CronSchedule(QUARTZ_CRON_PARSER.parse(cronExpression));
	}
}
