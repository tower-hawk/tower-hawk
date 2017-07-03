package org.towerhawk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.towerhawk.monitor.MonitorService;
import org.towerhawk.monitor.reader.CheckWatcher;

import javax.inject.Inject;

@RestController
@RequestMapping(path = "/refresh")
public class CheckRefresherController {

	private final CheckWatcher checkWatcher;
	private final MonitorService monitorService;
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	public CheckRefresherController(CheckWatcher checkWatcher, MonitorService monitorService) {
		this.checkWatcher = checkWatcher;
		this.monitorService = monitorService;
	}

	@RequestMapping(path = {"/", ""}, method = RequestMethod.POST)
	public void refresh() {
		monitorService.refreshDefinitions();
	}

	@RequestMapping(path = "/watcher", method = {RequestMethod.POST, RequestMethod.PUT})
	public void startWatcher(@RequestParam(required = false, defaultValue = "false", name = "restart") String restart) {
		//TODO: Get restarting to work
		if (Boolean.valueOf(restart)) {
			checkWatcher.stop();
		}
		checkWatcher.start();
	}

	@RequestMapping(path = "/watcher", method = RequestMethod.DELETE)
	public void stopWatcher() {
		checkWatcher.stop();
	}

	@RequestMapping(path = "/watcherRunning")
	public boolean watcherRunning() {
		return checkWatcher.running();
	}

}
