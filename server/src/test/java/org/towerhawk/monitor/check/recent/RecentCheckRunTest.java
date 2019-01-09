package org.towerhawk.monitor.check.recent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.TestCheck;
import org.towerhawk.monitor.check.run.CheckRun;

import java.util.List;
import java.util.stream.IntStream;

public class RecentCheckRunTest {

	private Check check;
	private CheckRun checkRun;

	@Before
	public void setup() {
		check = new TestCheck("RecentCheckRunTest");
		checkRun = CheckRun.builder(check).message("Testing RecentCheckRun").succeeded().build();
	}

	@Test
	public void testOrder() {
		List<CheckRun> checkRuns = generateCheckRuns(20, 10);
		Assert.assertEquals("CheckRun 0 should be first", "0", checkRuns.get(0).getMessage());
		Assert.assertEquals("CheckRun 9 should be last", "9", checkRuns.get(9).getMessage());
	}

	@Test
	public void testSizeLimit() {
		List<CheckRun> checkRuns = generateCheckRuns(5, 10);
		Assert.assertEquals("CheckRun 5 should be first", "5", checkRuns.get(0).getMessage());
		Assert.assertEquals("CheckRun 9 should be last", "9", checkRuns.get(4).getMessage());
	}

	@Test
	public void testCopyOrder() {
		List<CheckRun> checkRuns = generateCheckRuns(20, 10);
		RecentCheckRun recentCheckRun = new RecentCheckRun().setDefaultCheckRun(checkRun);
		checkRuns.forEach(checkRun -> recentCheckRun.addCheckRun(checkRun));
		checkRuns = recentCheckRun.getRecentCheckRuns();
		Assert.assertEquals("CheckRun 0 should be first", "0", checkRuns.get(0).getMessage());
		Assert.assertEquals("CheckRun 9 should be last", "9", checkRuns.get(9).getMessage());
	}

	private List<CheckRun> generateCheckRuns(int sizeLimit, int count) {
		RecentCheckRun recentCheckRun = new RecentCheckRun().setDefaultCheckRun(checkRun).setSizeLimit(sizeLimit);
		IntStream.range(0, count).forEach(i -> {
			CheckRun.Builder builder = CheckRun.builder(check);
			builder.message(String.valueOf(i));
			recentCheckRun.addCheckRun(builder.build());
		});
		return recentCheckRun.getRecentCheckRuns();
	}
}
