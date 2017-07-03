package org.towerhawk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.towerhawk.monitor.MonitorService;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.run.CheckRun;

import javax.inject.Inject;

@RestController
@RequestMapping(path = "/check")
public class CheckRunnerController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final MonitorService monitorService;

	@Inject
	public CheckRunnerController(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	@RequestMapping(path = {"", "/", "/app", "/app/"}, method = {RequestMethod.GET})
	public ResponseEntity<CheckRun> getRecentCheckServieRun() {
		return getCheckRunResponseEntity(monitorService, false);
	}

	@RequestMapping(path = {"", "/", "/app", "/app/"}, method = {RequestMethod.POST})
	public ResponseEntity<CheckRun> runCheckService() {
		return getCheckRunResponseEntity(monitorService, true);
	}

	@RequestMapping(path = "/app/{appId}", method = {RequestMethod.GET})
	public ResponseEntity<CheckRun> getRecentRunsByApp(@PathVariable(value = "appId") String appId) {
		return getCheckRunResponseEntity(monitorService.getApp(appId), false);
	}

	@RequestMapping(path = "/app/{appId}", method = {RequestMethod.POST})
	public ResponseEntity<CheckRun> runByApp(@PathVariable(value = "appId") String appId) {
		return getCheckRunResponseEntity(monitorService.getApp(appId), true);
	}

	private ResponseEntity<CheckRun> getCheckRunResponseEntity(App app, boolean runChecks) {
		CheckRun checkRun = null;
		if (app == null) {
			return new ResponseEntity<CheckRun>(checkRun, HttpStatus.BAD_REQUEST);
		}
		if (runChecks) {
			checkRun = app.run();
		} else {
			checkRun = app.getLastCheckRun();
		}
		HttpStatus status;
		if (checkRun.getStatus() == CheckRun.Status.SUCCEEDED) {
			status = HttpStatus.OK;
		} else {
			status = HttpStatus.SERVICE_UNAVAILABLE;
		}
		return new ResponseEntity<CheckRun>(checkRun, status);
	}
}
