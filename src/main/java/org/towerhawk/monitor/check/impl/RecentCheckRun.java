package org.towerhawk.monitor.check.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.towerhawk.monitor.check.run.CheckRun;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Accessors(chain = true)
public class RecentCheckRun {

	@Getter	private int sizeLimit;
	private Deque<CheckRun> recentCheckRuns;
	@Getter	@Setter	private CheckRun defaultCheckRun;

	public RecentCheckRun() {
		this.sizeLimit = 10;
		recentCheckRuns = new ArrayDeque<>(sizeLimit);
	}

	public RecentCheckRun setSizeLimit(int sizeLimit) {
		this.sizeLimit = sizeLimit;
		if (this.sizeLimit < 1) {
			this.sizeLimit = 1;
		}
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
		return new ArrayList<CheckRun>(recentCheckRuns);
	}
}
