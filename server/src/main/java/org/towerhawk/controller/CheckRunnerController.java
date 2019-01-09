package org.towerhawk.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.towerhawk.controller.exception.ResourceNotFoundException;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.filter.CheckFilter;
import org.towerhawk.monitor.check.logging.CheckMDC;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunSelector;
import org.towerhawk.monitor.check.run.Status;
import org.towerhawk.monitor.check.run.concurrent.ConcurrentCheckRunner;
import org.towerhawk.monitor.check.run.context.DefaultRunContext;
import org.towerhawk.spring.config.Configuration;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
public class CheckRunnerController {

	private final ControllerMonitorServiceWrapper monitorServiceWrapper;
	private final Configuration configuration;
	private final ConcurrentCheckRunner checkCheckRunner;
	private final ConcurrentCheckRunner appCheckRunner;
	private final ConcurrentCheckRunner monitorCheckRunner;
	private final Collection<Check> monitorCheck;

	@Inject
	public CheckRunnerController(
			ControllerMonitorServiceWrapper monitorServiceWrapper,
			Configuration configuration,
			ConcurrentCheckRunner checkCheckRunner,
			ConcurrentCheckRunner appCheckRunner,
			ConcurrentCheckRunner monitorCheckRunner
	) {
		this.monitorServiceWrapper = monitorServiceWrapper;
		this.configuration = configuration;
		this.checkCheckRunner = checkCheckRunner;
		this.appCheckRunner = appCheckRunner;
		this.monitorCheckRunner = monitorCheckRunner;
		monitorCheck = Collections.singletonList(monitorServiceWrapper.getMonitorService().getContainingCheck());
	}

	@RequestMapping(path = "/app", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<CheckRun> runApps(
			@RequestParam(required = false) List<Integer> priority,
			@RequestParam(required = false) Integer priorityLte,
			@RequestParam(required = false) Integer priorityGte,
			@RequestParam(required = false) List<String> tags,
			@RequestParam(required = false) List<String> notTags,
			@RequestParam(required = false) List<String> type,
			@RequestParam(required = false) List<String> notType,
			@RequestParam(required = false) List<String> id,
			@RequestParam(required = false) List<String> notId,
			@RequestParam(required = false) List<CheckRunSelector.Field> fields,
			HttpServletRequest request
	) {
		CheckFilter checkFilter = new CheckFilter(priority, priorityLte, priorityGte, tags, notTags, type, notType, id, notId);
		DefaultRunContext checkContext = monitorServiceWrapper.getContext(request);
		checkContext.putContext(monitorServiceWrapper.getMonitorService().predicateKey(), checkFilter);
		CheckRun checkRun = monitorCheckRunner.runChecks(monitorCheck, checkContext).get(0);
		return getCheckRunResponseEntity(checkRun, fields);
	}

	@RequestMapping(path = "/app/{appId}", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<CheckRun> runApp(
			@PathVariable String appId,
			@RequestParam(required = false) List<CheckRunSelector.Field> fields,
			HttpServletRequest request
	) {
		App app = monitorServiceWrapper.getApp(appId);
		DefaultRunContext checkContext = monitorServiceWrapper.getContext(request);
		CheckRun checkRun = appCheckRunner.runChecks(Arrays.asList(app.getContainingCheck()), checkContext).get(0);
		return getCheckRunResponseEntity(checkRun, fields);
	}

	@RequestMapping(path = "/app/{appId}/{checkId}", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<CheckRun> runCheck(
			@PathVariable String appId,
			@PathVariable String checkId,
			@RequestParam(required = false) List<CheckRunSelector.Field> fields,
			HttpServletRequest request
	) {
		Check check = monitorServiceWrapper.getCheck(appId, checkId);
		DefaultRunContext checkContext = monitorServiceWrapper.getContext(request);
		CheckRun checkRun = checkCheckRunner.runChecks(Arrays.asList(check), checkContext).get(0);
		return getCheckRunResponseEntity(checkRun, fields);
	}

	@RequestMapping(path = {"/", "/check"}, method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<CheckRun> runChecks(
			@RequestParam(required = false) List<Integer> priority,
			@RequestParam(required = false) Integer priorityLte,
			@RequestParam(required = false) Integer priorityGte,
			@RequestParam(required = false) List<String> tags,
			@RequestParam(required = false) List<String> notTags,
			@RequestParam(required = false) List<String> type,
			@RequestParam(required = false) List<String> notType,
			@RequestParam(required = false) List<String> id,
			@RequestParam(required = false) List<String> notId,
			@RequestParam(required = false) List<Integer> appPriority,
			@RequestParam(required = false) Integer appPriorityLte,
			@RequestParam(required = false) Integer appPriorityGte,
			@RequestParam(required = false) List<String> appTags,
			@RequestParam(required = false) List<String> appNotTags,
			@RequestParam(required = false) List<String> appType,
			@RequestParam(required = false) List<String> appNotType,
			@RequestParam(required = false) List<String> appId,
			@RequestParam(required = false) List<String> appNotId,
			@RequestParam(required = false) List<CheckRunSelector.Field> fields,
			HttpServletRequest request
	) {
		CheckFilter checkFilter = new CheckFilter(priority, priorityLte, priorityGte, tags, notTags, type, notType, id, notId);
		CheckFilter appFilter = new CheckFilter(appPriority, appPriorityLte, appPriorityGte, appTags, appNotTags, appType, appNotType, appId, appNotId);
		DefaultRunContext checkContext = monitorServiceWrapper.getContext(request);
		checkContext.putContext(monitorServiceWrapper.getMonitorService().appPredicateKey(), checkFilter);
		checkContext.putContext(monitorServiceWrapper.getMonitorService().predicateKey(), appFilter);
		CheckRun checkRun = monitorCheckRunner.runChecks(monitorCheck, checkContext).get(0);
		return getCheckRunResponseEntity(checkRun, fields);
	}

	@RequestMapping("/apps")
	public Collection<String> getAppNames() {
		return monitorServiceWrapper.getMonitorService().getCheckNames();
	}

	@RequestMapping("/apps/{appId}")
	public Collection<String> getCheckNames(@PathVariable String appId) {
		App app = monitorServiceWrapper.getApp(appId);
		return app.getCheckNames();
	}

	private ResponseEntity<CheckRun> getCheckRunResponseEntity(CheckRun checkRun, Collection<CheckRunSelector.Field> fields) {
		int responseCode = getResponseCode(checkRun.getStatus());
		if (fields == null || fields.isEmpty()) {
			fields = configuration.getCheckRunDefaultFields();
		}
		checkRun = new CheckRunSelector(checkRun, fields);
		//Clear the CheckMDC to ensure that we don't end up in a weird state
		//Do it here since this is always the last method called.
		CheckMDC.clear();
		return ResponseEntity.status(responseCode).body(checkRun);
	}

	private int getResponseCode(Status status) {
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
