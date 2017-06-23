package org.towerhawk.check;

import org.towerhawk.check.run.CheckRun;
import org.towerhawk.spring.Configuration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class RecentCheckRun {

	private int sizeLimit = Configuration.get().getRecentChecksSizeLimit();
	private Deque<CheckRun> recentCheckRuns = new ArrayDeque<>(sizeLimit);

	private int getSizeLimit() {
		return sizeLimit;
	}

	private void setSizeLimit(int sizeLimit) {
		this.sizeLimit = sizeLimit;
	}

	public void addCheckRun(CheckRun checkRun) {
		if (recentCheckRuns.size() > sizeLimit) {
			recentCheckRuns.removeFirst();
		}
		if (checkRun != null) {
			recentCheckRuns.addLast(checkRun);
		}
	}

	public CheckRun getLastRun() {
		return recentCheckRuns.peekLast();
	}

	public List<CheckRun> getRecentCheckRuns() {
		return new ArrayList<CheckRun>(recentCheckRuns);
	}
}
