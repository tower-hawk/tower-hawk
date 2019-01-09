package org.towerhawk.monitor.reader;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.towerhawk.monitor.app.App;
import org.towerhawk.plugin.TowerhawkPluginManager;
import org.towerhawk.serde.resolver.ExtensibleAPI;
import org.towerhawk.serde.resolver.TowerhawkIgnore;
import org.towerhawk.serde.resolver.TowerhawkType;
import org.towerhawk.spring.config.Configuration;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Named
public class CheckRefresher {
	@Getter
	private Path definitionsDir;
	private ObjectMapper mapper;
	private TowerhawkPluginManager tpm;

	@Inject
	public CheckRefresher(Configuration configuration, TowerhawkPluginManager towerhawkPluginManager) {
		this(configuration.getCheckDefinitionDir(), towerhawkPluginManager);
	}

	public CheckRefresher(String definitionsDir, TowerhawkPluginManager towerhawkPluginManager) {
		this.tpm = towerhawkPluginManager;
		this.definitionsDir = Paths.get(definitionsDir);

		mapper = initalizeObjectMapper();

		SimpleModule module = getModule();

		mapper.registerModule(module);
	}

	private SimpleModule getModule() {
		SimpleModule module =
				new SimpleModule("TowerhawkDeserializerModule",
						new Version(1, 0, 0, null, null, null));

		PluginManager pluginManager = tpm.getPluginManager();
		Map<Class, TowerhawkDeserializer> deserializerMap = new HashMap<>();

		ExtensibleAPI.CLASSES.forEach(apiClass -> {
			TowerhawkDeserializer deserializer = new TowerhawkDeserializer<>(apiClass);
			deserializerMap.put(apiClass, deserializer);
			List<?> apiClasses = pluginManager.getExtensionClasses(apiClass);
			apiClasses.forEach(clazz ->	registerClass(apiClass, deserializer, (Class)clazz));
		});

		log.info("Finished scanning for classes");

		deserializerMap.forEach((k, v) -> {
			v.defaultName("default");
			module.addDeserializer(k, v);
		});
		return module;
	}

	private void registerClass(Class<? extends ExtensionPoint> apiClass, TowerhawkDeserializer deserializer, Class<?> c) {
		int mod = c.getModifiers();
		if (!Modifier.isInterface(mod) && !Modifier.isAbstract(mod) && !shouldIgnoreClass(c, apiClass)) {
			TowerhawkType t = c.getAnnotation(TowerhawkType.class);
			if (t != null) {
				for (String v : t.value()) {
					deserializer.register(v, c);
				}
			}
			deserializer.register(c.getSimpleName(), c);
			deserializer.register(c.getCanonicalName(), c);
		}
	}

	protected ObjectMapper initalizeObjectMapper() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);
		mapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
		mapper.enable(JsonParser.Feature.IGNORE_UNDEFINED);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		TypeFactory typeFactory = TypeFactory.defaultInstance();
		for (PluginWrapper p : tpm.getPluginManager().getPlugins()) {
			typeFactory = typeFactory.withClassLoader(p.getPluginClassLoader());
		}
		mapper.setTypeFactory(typeFactory);
		return mapper;
	}

	protected boolean shouldIgnoreClass(Class<?> c, Class<?> apiClass) {
		if (c == null || Object.class.equals(c)) {
			return false;
		}
		TowerhawkIgnore ignore = c.getAnnotation(TowerhawkIgnore.class);
		if (ignore != null) {
			for (Class compare : ignore.value()) {
				if (c.equals(compare)) {
					return true;
				} else if (apiClass.equals(compare)) {
					return true;
				}
			}
		}
		for (Class i : c.getInterfaces()) {
			if (shouldIgnoreClass(i, apiClass)) {
				return true;
			}
		}
		return shouldIgnoreClass(c.getSuperclass(), apiClass);
	}

	public static boolean validFile(File file) {
		return file.toString().endsWith(".yaml") || file.toString().endsWith(".yml") || file.toString().endsWith(".json");
	}

	@Synchronized
	public CheckDTO readDefinitions() {
		List<CheckDTO> checkDTOs = new ArrayList<>();
		File[] files = definitionsDir.toFile().listFiles(CheckRefresher::validFile);
		if (files == null) {
			log.warn("No checks defined!");
		} else {
			for (File file : files) {
				if (validFile(file)) {
					try {
						log.info("Refreshing file {}", file);
						checkDTOs.add(mapper.readValue(file, CheckDTO.class));
					} catch (JsonMappingException e) {
						if (e.getMessage().startsWith("No content to map")) {
							log.warn("Excluding file {} because no input is available due to exception {}", file.toString(), e.getMessage());
						} else {
							throw new IllegalArgumentException("Unable to deserialize yaml file " + file.toString() + " to object structure", e);
						}
					} catch (Exception e) {
						log.error("Failed to deserialize yaml file {}", file.toString(), e);
						throw new IllegalArgumentException("Failed to deserialize yaml file " + file.toString(), e);
					}
				}
			}
		}
		return mergeChecks(checkDTOs);
	}

	private CheckDTO mergeChecks(List<CheckDTO> checkDTOS) {
		Map<String, App> apps = new LinkedHashMap<>();
		checkDTOS.forEach(dtoConsumers ->
				dtoConsumers.getApps().forEach((name, app) -> {
			apps.compute(name, (k, v) -> {
				if (v != null) {
					throw new IllegalArgumentException("App " + k + " is defined multiple times within the configuration files");
				}
				return app;
			});
		}));
		return new CheckDTO(apps);
	}
}
