package org.towerhawk.monitor.check.impl;

import org.towerhawk.monitor.check.run.CheckRun;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class RecentCheckRun {

	private int sizeLimit;
	private Deque<CheckRun> recentCheckRuns = new ArrayDeque<>(sizeLimit);
	private transient CheckRun defaultCheckRun;

	public RecentCheckRun(int sizeLimit, CheckRun defaultCheckRun) {
		this.sizeLimit = sizeLimit;
		this.defaultCheckRun = defaultCheckRun;
	}

	private int getSizeLimit() {
		return sizeLimit;
	}

	private void setSizeLimit(int sizeLimit) {
		this.sizeLimit = sizeLimit;
		if (this.sizeLimit < 1) {
			this.sizeLimit = 1;
		}
	}

	public void addCheckRun(CheckRun checkRun) {
		if (checkRun != null) {
			if (recentCheckRuns.size() >= sizeLimit) {
				CheckRun removing = recentCheckRuns.removeFirst();
				removing.cleanUp();
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
