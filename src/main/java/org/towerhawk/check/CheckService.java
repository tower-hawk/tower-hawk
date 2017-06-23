package org.towerhawk.check;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.check.app.App;
import org.towerhawk.check.reader.CheckDeserializer;
import org.towerhawk.check.reader.CheckPostProcessor;
import org.towerhawk.check.reader.CheckRefresher;
import org.towerhawk.check.run.CheckRunner;
import org.towerhawk.spring.Configuration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Collections;

@Named
public class CheckService extends App {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Configuration configuration;

	@Inject
	public CheckService(Configuration configuration, CheckRunner checkRunner) {
		this.configuration = configuration;
		this.checkRunner = checkRunner;
		type = "CheckService";
		//TODO get a separate checkrunner to run apps on
	}

	@PostConstruct
	protected void init() {
		refreshDefinitions();

	}

	public boolean refreshDefinitions() {
		try {
			CheckDeserializer newDefs = new CheckRefresher(configuration.getCheckDefinitionDir()).readDefinitions();
			CheckPostProcessor postProcessor = new CheckPostProcessor(newDefs, checks, checkRunner);
			CheckDeserializer processedDefs = postProcessor.postProcess();
			checks = Collections.unmodifiableMap(processedDefs.getApps());
		} catch (RuntimeException e) {
			log.error("Failed to load new checks", e);
			return false;
		}
		return true;
	}

	public App getApp(String appId) {
		return (App) checks.get(appId);
	}

	public Collection<String> getAppNames() {
		return checks.keySet();
	}

}
