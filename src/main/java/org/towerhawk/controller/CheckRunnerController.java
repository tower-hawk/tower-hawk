package org.towerhawk.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.towerhawk.monitor.MonitorService;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunSelector;
import org.towerhawk.spring.config.Configuration;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/check")
public class CheckRunnerController {

	private final MonitorService monitorService;
	private final Configuration configuration;

	@Inject
	public CheckRunnerController(MonitorService monitorService, Configuration configuration) {
		this.monitorService = monitorService;
		this.configuration = configuration;
	}

	@RequestMapping(method = {RequestMethod.GET})
	public ResponseEntity<CheckRun> getRecentCheckServieRun(
		@RequestParam(value = "fields", required = false) List<CheckRunSelector.Field> fields) {
		return getCheckRunResponseEntity(monitorService, false, fields);
	}

	@RequestMapping(method = {RequestMethod.POST})
	public ResponseEntity<CheckRun> runCheckService(
		@RequestParam(value = "fields", required = false) List<CheckRunSelector.Field> fields) {
		return getCheckRunResponseEntity(monitorService, true, fields);
	}

	@RequestMapping(path = "/app/{appId}", method = {RequestMethod.GET})
	public ResponseEntity<CheckRun> getRecentRunsByApp(
		@PathVariable(value = "appId") String appId,
		@RequestParam(value = "fields", required = false) List<CheckRunSelector.Field> fields) {
		return getCheckRunResponseEntity(monitorService.getApp(appId), false, fields);
	}

	@RequestMapping(path = "/app/{appId}", method = {RequestMethod.POST})
	public ResponseEntity<CheckRun> runByApp(
		@PathVariable(value = "appId") String appId,
		@RequestParam(value = "fields", required = false) List<CheckRunSelector.Field> fields) {
		return getCheckRunResponseEntity(monitorService.getApp(appId), true, fields);
	}

	private ResponseEntity<CheckRun> getCheckRunResponseEntity(Check app, boolean runChecks, Collection<CheckRunSelector.Field> fields) {
		CheckRun checkRun = null;
		if (app == null) {
			return new ResponseEntity<>(checkRun, HttpStatus.BAD_REQUEST);
		}
		if (runChecks) {
			checkRun = app.run();
		} else {
			checkRun = app.getLastCheckRun();
		}
		int responseCode = getResponseCode(checkRun.getStatus());
		checkRun = new CheckRunSelector(checkRun, fields, configuration);
		return ResponseEntity.status(responseCode).body(checkRun);
	}

	private int getResponseCode(CheckRun.Status status) {
		int responseCode;
		switch (status) {
			case SUCCEEDED:
				responseCode = configuration.getSucceededResponseCode();
				break;
			case UNKNOWN:
				responseCode = configuration.getUnknownResponseCode();
				break;
			case WARNING:
				responseCode = configuration.getWarningResponseCode();
				break;
			default:
				responseCode = configuration.getCriticalResponseCode();
		}
		return responseCode;
	}
}
