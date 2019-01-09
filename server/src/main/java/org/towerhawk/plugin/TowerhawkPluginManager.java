package org.towerhawk.plugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.towerhawk.spring.config.Configuration;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Paths;

@Getter
@Slf4j
@Named
public class TowerhawkPluginManager {

	protected PluginManager pluginManager;

	@Inject
	public TowerhawkPluginManager(Configuration configuration) {
		pluginManager = new DefaultPluginManager(Paths.get(configuration.getPluginsDir()));
		Runtime.getRuntime().addShutdownHook(new Thread(() -> pluginManager.stopPlugins()));
		pluginManager.loadPlugins();
		pluginManager.startPlugins();
		PluginContext.setPluginManager(pluginManager);
	}
}
