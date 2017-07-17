package org.towerhawk.monitor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.active.Enabled;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.CheckContext;
import org.towerhawk.monitor.check.DefaultCheckContext;
import org.towerhawk.monitor.check.run.CheckRunner;
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
import java.util.LinkedHashMap;

@Slf4j
@Named
public class MonitorService extends App {

	private final CheckRunner checkCheckRunner;
	private final CheckContext checkContext = new DefaultCheckContext();
	@Getter
	private ZonedDateTime lastRefresh = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());

	@Inject
	public MonitorService(Configuration configuration,
												ConcurrentCheckRunner checkCheckRunner,
												ConcurrentCheckRunner appCheckRunner) {
		setConfiguration(configuration);
		setCheckRunner(appCheckRunner);
		this.checkCheckRunner = checkCheckRunner;
		setType("MonitorService");
		checks = new LinkedHashMap<>();
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
			run(checkContext);
		}
	}

	public boolean refreshDefinitions() {
		try {
			CheckDeserializer newDefs = new CheckRefresher(getConfiguration().getCheckDefinitionDir()).readDefinitions();
			postProcess(newDefs);
			checks = Collections.unmodifiableMap(newDefs.getApps());
			if (getConfiguration().isRunChecksOnRefresh()) {
				run(checkContext);
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
		setActive(new Enabled());
		super.init(check, configuration, app, id);
		setTimeoutMs(configuration.getHardTimeoutLimit());
		setPriority(Byte.MAX_VALUE);

	}

	@Override
	public boolean canRun() {
		return true;
	}

	@Override
	public String predicateKey() {
		return "appPredicate";
	}

	public String appPredicateKey() {
		return super.predicateKey();
	}

	private void postProcess(CheckDeserializer checkDeserializer) {
		Collection<Check> appsToClose = new ArrayList<>();
		//initialize all checks first
		checkDeserializer.getApps().forEach((id, app) -> {
			app.setCheckRunner(checkCheckRunner);
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
				log.error("App {} failed to close with exception", previousApp.getFullName(), e);
			}
		});
	}

}
