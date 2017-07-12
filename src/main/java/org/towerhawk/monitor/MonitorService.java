package org.towerhawk.monitor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

@Slf4j
@Named
public class MonitorService extends App {

	private final CheckRunner checkRunnerForChecks;
	@Getter
	private ZonedDateTime lastRefresh = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());

	@Inject
	public MonitorService(Configuration configuration
		, ConcurrentCheckInterruptor interruptor
		, ExecutorService checkRunService
		, ExecutorService appRunService) {
		//Require configuration so Spring starts it up after the config is available
		setConfiguration(configuration);
		CheckRunner appRunner = new ConcurrentCheckRunner(interruptor, appRunService);
		setCheckRunner(appRunner);
		checkRunnerForChecks = new ConcurrentCheckRunner(interruptor, checkRunService);
		setType("CheckService");
	}

	@PostConstruct
	public void postConstruct() {
		init(null, getConfiguration(), this, "monitorService");

		boolean refreshed = refreshDefinitions();
		if (!refreshed && getConfiguration().isShutdownOnInitializationFailure()) {
			log.error("##################################################################");
			log.error("##########  Unable to initialize checks. Shutting down  ##########");
			log.error("##################################################################");
			System.exit(1);
		}
		if (refreshed && getConfiguration().isRunChecksOnStartup() && !getConfiguration().isRunChecksOnRefresh()) {
			run();
		}
	}

	public boolean refreshDefinitions() {
		try {
			CheckDeserializer newDefs = new CheckRefresher(getConfiguration().getCheckDefinitionDir()).readDefinitions();
			postProcess(newDefs);
			checks = Collections.unmodifiableMap(newDefs.getApps());
			if (getConfiguration().isRunChecksOnRefresh()) {
				run();
			}
			lastRefresh = ZonedDateTime.now();
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
		setCacheMs(0);
		setTimeoutMs(0);
		setPriority(Integer.MAX_VALUE);
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
			app.init(previousApp, getConfiguration(), this, id);
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
