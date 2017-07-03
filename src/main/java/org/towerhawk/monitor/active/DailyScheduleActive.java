package org.towerhawk.monitor.active;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DailyScheduleActive implements ActiveCheck {

	private static final Logger log = LoggerFactory.getLogger(DailyScheduleActive.class);
	private LocalTime startTime;
	private LocalTime endTime;
	private DateTimeFormatter timeFormat;

	@JsonCreator
	public DailyScheduleActive(@JsonProperty("startTime") String startTime
		, @JsonProperty("endTime") String endTime
		, @JsonProperty("timeFormat") String timeFormat) {
		if (timeFormat == null || timeFormat.isEmpty()) {
			timeFormat = "HH:mm";
		}
		try {
			this.timeFormat = DateTimeFormatter.ofPattern(timeFormat);
			this.startTime = LocalTime.parse(startTime, this.timeFormat);
			this.endTime = LocalTime.parse(endTime, this.timeFormat);
		} catch (Exception e) {
			log.error("Unable to initialize start or end time", e);
		}
	}

	@Override
	public boolean isActive() {
		LocalTime now = LocalTime.now();
		boolean nowBetweenStartAndEnd = startTime.compareTo(now) < 0 && now.compareTo(endTime) < 0;
		if (startTime.compareTo(endTime) < 0) {
			return nowBetweenStartAndEnd;
		} else { //Start is before midnight and end is after midnight or they are equal
			return !nowBetweenStartAndEnd;
		}
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public DateTimeFormatter getTimeFormat() {
		return timeFormat;
	}
}
