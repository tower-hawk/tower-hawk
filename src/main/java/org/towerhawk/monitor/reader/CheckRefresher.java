package org.towerhawk.monitor.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.app.App;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CheckRefresher {
	private String definitionsDir;

	public static boolean validFile(File file) {
		return file.toString().endsWith(".yaml") || file.toString().endsWith(".yml");
	}

	public CheckRefresher(String definitionsDir) {
		this.definitionsDir = definitionsDir;
	}

	public CheckDeserializer readDefinitions() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		File definitionsDir = Paths.get(this.definitionsDir).toFile();
		List<CheckDeserializer> checkDeserializers = new ArrayList<>();
		for (File file : definitionsDir.listFiles()) {
			if (validFile(file)) {
				try {
					log.info("Refreshing file {}", file);
					checkDeserializers.add(mapper.readValue(file, CheckDeserializer.class));
				} catch (Exception e) {
					log.error("Failed to deserialize yaml file {}", file.toPath().toString(), e);
					throw new RuntimeException("Unable to read yaml files from " + file.toPath().toString() + " directory", e);
				}
			}
		}
		return mergeChecks(checkDeserializers);
	}

	private CheckDeserializer mergeChecks(List<CheckDeserializer> checkDeserializers) {
		Map<String, App> apps = new LinkedHashMap<>();
		checkDeserializers.stream().forEachOrdered(dtoConsumer -> {
			apps.putAll(dtoConsumer.getApps());
		});
		return new CheckDeserializer(apps);
	}
}
