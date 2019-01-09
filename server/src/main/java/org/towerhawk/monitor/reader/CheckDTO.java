package org.towerhawk.monitor.reader;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.Setter;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.app.DefaultApp;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class CheckDTO {

	private Map<String, App> apps = new LinkedHashMap<>();

	public CheckDTO() {}

	public CheckDTO(Map<String, App> apps) {
		this.apps.putAll(apps);
	}

	@JsonAnySetter
	public void addApp(String name, App app) {
		apps.put(name, app);
	}
}
