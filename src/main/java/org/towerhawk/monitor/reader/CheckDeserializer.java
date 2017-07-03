package org.towerhawk.monitor.reader;

import org.towerhawk.monitor.app.App;

import java.util.Map;

public class CheckDeserializer {

	private Map<String, App> apps;

	public CheckDeserializer() {
		//for jackson
	}

	public CheckDeserializer(Map<String, App> apps) {
		this.apps = apps;
	}

	public Map<String, App> getApps() {
		return apps;
	}

	public void setApps(Map<String, App> apps) {
		this.apps = apps;
	}
}
