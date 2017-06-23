package org.towerhawk.check.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.check.Check;
import org.towerhawk.check.app.App;
import org.towerhawk.check.run.CheckRunner;

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

	public CheckDeserializer postProcess() {
		checkDeserializer.getApps().forEach((id, app) -> {
			app.setId(id);
			app.setApp(app);
			app.setCheckRunner(checkRunner);
			App previousApp = (App) apps.get(app.getId());
			app.init(previousApp);
			if(previousApp != null) {
				try {
					previousApp.close();
				} catch (IOException e) {
					log.error("App {} failed to close with error", app.getId(), e);
				}
			}
		});
		return checkDeserializer;
	}
}
