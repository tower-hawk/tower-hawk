package org.towerhawk.monitor.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRunner;

import java.io.IOException;
import java.util.Map;

public class CheckPostProcessor {

	private static final Logger log = LoggerFactory.getLogger(CheckPostProcessor.class);
	private CheckDeserializer checkDeserializer;
	private Map<String, Check> apps;
	private CheckRunner checkRunner;

	public CheckPostProcessor(CheckDeserializer checkDeserializer, Map<String, Check> apps, CheckRunner checkRunner) {
		this.checkDeserializer = checkDeserializer;
		this.apps = apps;
		this.checkRunner = checkRunner;
	}


}
