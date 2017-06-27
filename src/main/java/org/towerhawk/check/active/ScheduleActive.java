package org.towerhawk.check.active;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

public class ScheduleActive implements ActiveCheck{

	LocalTime startTime = null;
	LocalTime endTime = null;
	int startHour;
	int startMinute;
	int startSecond;
	int endHour;
	int endMinute;
	int endSecond;

	@JsonCreator
	public ScheduleActive(@JsonProperty("startHour") int startHour
		, @JsonProperty("startMinute") int startMinute
		, @JsonProperty("startSecond") int startSecond
		, @JsonProperty("endHour") int endHour
		, @JsonProperty("endMinute") int endMinute
		, @JsonProperty("endSecond") int endSecond) {
		startTime = LocalTime.of(startHour, startMinute, startSecond);
		endTime = LocalTime.of(endHour, endMinute, endSecond);
	}

	@Override
	public boolean isActive() {
		LocalTime now = LocalTime.now();
		if (startTime == null || endTime == null) {
			return false;
		}
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

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public int getStartHour() {
		return startHour;
	}

	public void setStartHour(int startHour) {
		this.startHour = startHour;
	}

	public int getStartMinute() {
		return startMinute;
	}

	public void setStartMinute(int startMinute) {
		this.startMinute = startMinute;
	}

	public int getStartSecond() {
		return startSecond;
	}

	public void setStartSecond(int startSecond) {
		this.startSecond = startSecond;
	}

	public int getEndHour() {
		return endHour;
	}

	public void setEndHour(int endHour) {
		this.endHour = endHour;
	}

	public int getEndMinute() {
		return endMinute;
	}

	public void setEndMinute(int endMinute) {
		this.endMinute = endMinute;
	}

	public int getEndSecond() {
		return endSecond;
	}

	public void setEndSecond(int endSecond) {
		this.endSecond = endSecond;
	}
}
