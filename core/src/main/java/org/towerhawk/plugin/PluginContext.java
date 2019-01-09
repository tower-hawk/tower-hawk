package org.towerhawk.plugin;

import lombok.AccessLevel;
import lombok.Setter;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

public class PluginContext {

	@Setter(AccessLevel.MODULE)
	protected static PluginManager pluginManager;

	public static ClassLoader getClassLoader(Class c) {
		if (pluginManager != null) {
			PluginWrapper wrapper = pluginManager.whichPlugin(c);
			return pluginManager.getPluginClassLoader(wrapper.getPluginId());
		} else {
			return null;
		}
	}
}
