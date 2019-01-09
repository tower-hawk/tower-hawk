package org.towerhawk.plugin;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;

@Slf4j
public class TowerhawkPlugin extends Plugin {
	public TowerhawkPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void start() throws PluginException {
		log.info("Starting plugin {}", wrapper.getPluginId());
	}

	@Override
	public void stop() throws PluginException {
		log.info("Stopping plugin {}", wrapper.getPluginId());
	}
}
