package org.towerhawk.monitor.active;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Extension;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Getter
@Extension
@TowerhawkType("daily")
public class DailyActive implements Active {

	private LocalTime startTime;
	private LocalTime endTime;
	private DateTimeFormatter timeFormat;

	@JsonCreator
	public DailyActive(@JsonProperty("startTime") String startTime
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
}
