package org.towerhawk.plugin.cron;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.towerhawk.serde.resolver.TowerhawkType;

import static org.junit.jupiter.api.Assertions.*;

class CronScheduleCollectorTest {

	private CronScheduleCollector schedule;
	private String cron = "0 * * * * ? *";

	@BeforeEach
	void setUp() {
		schedule = new CronScheduleCollector(cron, null, "quartz");
	}

	@Test
	void getCron() {
		String c = schedule.getCron();
		assertEquals(cron, c);
	}
}