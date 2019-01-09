package org.towerhawk.monitor.check.recent;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.run.CheckRun;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Slf4j
@Accessors(chain = true)
public class RecentCheckRun {

	private static String propertyKey = "org.towerhawk.check.recentChecks.sizeLimit";
	private static int defaultSizeLimit;

	static {
		try {
			defaultSizeLimit = Integer.valueOf(System.getProperty(propertyKey, "10"));
		} catch (Exception e) {
			log.error("Unable to read property {} defaulting to 10", propertyKey, e);
			defaultSizeLimit = 10;
		}
	}

	@Getter
	private int sizeLimit;
	private Deque<CheckRun> recentCheckRuns;
	@Getter
	@Setter
	private CheckRun defaultCheckRun;

	public RecentCheckRun() {
		sizeLimit = defaultSizeLimit;
		recentCheckRuns = new ArrayDeque<>(sizeLimit);
	}

	public RecentCheckRun setSizeLimit(int newSizelimit) {
		sizeLimit = Math.max(newSizelimit, 1);
		while (recentCheckRuns.size() > sizeLimit) {
			recentCheckRuns.removeFirst().cleanUp();
		}
		return this;
	}

	public void addCheckRun(CheckRun checkRun) {
		if (checkRun != null) {
			//if size == 1, this prevents returning null
			defaultCheckRun = checkRun;
			if (recentCheckRuns.size() >= sizeLimit) {
				recentCheckRuns.removeFirst().cleanUp();
			}
			recentCheckRuns.addLast(checkRun);
		}
	}

	public CheckRun getLastRun() {
		CheckRun checkRun = recentCheckRuns.peekLast();
		if (checkRun == null) {
			checkRun = defaultCheckRun;
		}
		return checkRun;
	}

	public List<CheckRun> getRecentCheckRuns() {
		return new ArrayList<>(recentCheckRuns);
	}
}
