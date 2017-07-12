package org.towerhawk.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.towerhawk.monitor.MonitorService;
import org.towerhawk.monitor.reader.CheckWatcher;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/refresh")
public class CheckRefresherController {

	private final CheckWatcher checkWatcher;
	private final MonitorService monitorService;

	@Inject
	public CheckRefresherController(CheckWatcher checkWatcher, MonitorService monitorService) {
		this.checkWatcher = checkWatcher;
		this.monitorService = monitorService;
	}

	@RequestMapping
	public Map<String, Object> refreshInfo() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("running", checkWatcher.running());
		map.put("lastRefresh", monitorService.getLastRefresh());
		return map;
	}

	@RequestMapping(method = RequestMethod.POST)
	public Map<String, Object> refresh() {
		monitorService.refreshDefinitions();
		return refreshInfo();
	}

	@RequestMapping(path = "/watcher", method = {RequestMethod.POST, RequestMethod.PUT})
	public void startWatcher(@RequestParam(required = false, defaultValue = "false", name = "restart") boolean restart,
													 @RequestParam(required = false, defaultValue = "false", name = "refresh") boolean refresh) {
		if (restart) {
			stopWatcher();
		}
		checkWatcher.start();
		if (refresh) {
			refresh();
		}
	}

	@RequestMapping(path = "/watcher", method = RequestMethod.DELETE)
	public void stopWatcher() {
		checkWatcher.stop();
	}

	@RequestMapping(path = "/watcher")
	public boolean watcherRunning() {
		return checkWatcher.running();
	}

}
