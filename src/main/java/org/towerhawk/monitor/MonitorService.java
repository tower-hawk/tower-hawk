package org.towerhawk.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.monitor.active.Enabled;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.concurrent.ConcurrentCheckInterruptor;
import org.towerhawk.monitor.check.run.concurrent.ConcurrentCheckRunner;
import org.towerhawk.monitor.reader.CheckDeserializer;
import org.towerhawk.monitor.reader.CheckRefresher;
import org.towerhawk.spring.config.Configuration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

@Named
public class MonitorService extends App {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final CheckRunner checkRunnerForChecks;

	@Inject
	public MonitorService(Configuration configuration
		, ConcurrentCheckInterruptor interruptor
		, ExecutorService checkRunService
	  , ExecutorService appRunService) {
		//Require configuration so Spring starts it up after the config is available
		this.configuration = configuration;
		CheckRunner appRunner = new ConcurrentCheckRunner(interruptor, appRunService);
		setCheckRunner(appRunner);
		checkRunnerForChecks = new ConcurrentCheckRunner(interruptor, checkRunService);
		type = "CheckService";
	}

	@PostConstruct
	public void postConstruct() {
		init(null, configuration, this, "monitorService");

		boolean refreshed = refreshDefinitions();
		if (!refreshed && configuration.isShutdownOnInitializationFailure()) {
			log.error("##################################################################");
			log.error("##########  Unable to initialize checks. Shutting down  ##########");
			log.error("##################################################################");
			System.exit(1);
		}
		if (refreshed && configuration.isRunChecksOnStartup() && !configuration.isRunChecksOnRefresh()) {
			run();
		}
	}

	public boolean refreshDefinitions() {
		try {
			CheckDeserializer newDefs = new CheckRefresher(configuration.getCheckDefinitionDir()).readDefinitions();
			postProcess(newDefs);
			checks = Collections.unmodifiableMap(newDefs.getApps());
			if (configuration.isRunChecksOnRefresh()) {
				run();
			}
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

	@Override
	public void init(Check check, Configuration configuration, App app, String id) {
		cacheMs = 0;
		timeoutMs = 0;
		priority = Integer.MAX_VALUE;
		setActive(new Enabled());
		super.init(check, configuration, app, id);
	}

	@Override
	public boolean canRun() {
		return true;
	}

	private void postProcess(CheckDeserializer checkDeserializer) {
		Collection<Check> appsToClose = new ArrayList<>();
		//initialize all checks first
		checkDeserializer.getApps().forEach((id, app) -> {
			app.setCheckRunner(checkRunnerForChecks);
			Check previousApp = checks.get(app.getId());
			app.init(previousApp, configuration, this, id);
			if (previousApp != null) {
				appsToClose.add(previousApp);
			}
		});
		//then close old checks
		appsToClose.forEach(previousApp -> {
			try {
				previousApp.close();
			} catch (IOException e) {
				log.error("App {} failed to close with exception", previousApp.getId(), e);
			}
		});
	}

}
