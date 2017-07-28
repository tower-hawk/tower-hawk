package org.towerhawk.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.towerhawk.controller.exception.ResourceNotFoundException;
import org.towerhawk.monitor.MonitorService;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.context.DefaultRunContext;
import org.towerhawk.monitor.check.filter.CheckFilter;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunSelector;
import org.towerhawk.monitor.check.run.concurrent.ConcurrentCheckRunner;
import org.towerhawk.spring.config.Configuration;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
public class CheckRunnerController {

	private final MonitorService monitorService;
	private final Configuration configuration;
	private final ConcurrentCheckRunner checkCheckRunner;
	private final ConcurrentCheckRunner appCheckRunner;
	private final ConcurrentCheckRunner monitorCheckRunner;
	private final Collection<Check> monitorCheck;

	@Inject
	public CheckRunnerController(
		MonitorService monitorService,
		Configuration configuration,
		ConcurrentCheckRunner checkCheckRunner,
		ConcurrentCheckRunner appCheckRunner,
		ConcurrentCheckRunner monitorCheckRunner
	) {
		this.monitorService = monitorService;
		this.configuration = configuration;
		this.checkCheckRunner = checkCheckRunner;
		this.appCheckRunner = appCheckRunner;
		this.monitorCheckRunner = monitorCheckRunner;
		monitorCheck = new ArrayList<>();
		monitorCheck.add(monitorService);
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
		DefaultRunContext checkContext = getContext(request);
		checkContext.putContext(monitorService.predicateKey(), checkFilter);
		CheckRun checkRun = monitorCheckRunner.runChecks(monitorCheck, checkContext).get(0);
		return getCheckRunResponseEntity(checkRun, fields);
	}

	@RequestMapping(path = "/app/{appId}", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<CheckRun> runApp(
		@PathVariable String appId,
		@RequestParam(required = false) List<CheckRunSelector.Field> fields,
		HttpServletRequest request
	) {
		App app = getApp(appId);
		DefaultRunContext checkContext = getContext(request);
		CheckRun checkRun = appCheckRunner.runChecks(Arrays.asList(app), checkContext).get(0);
		return getCheckRunResponseEntity(checkRun, fields);
	}

	@RequestMapping(path = "/app/{appId}/{checkId}", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<CheckRun> runCheck(
		@PathVariable String appId,
		@PathVariable String checkId,
		@RequestParam(required = false) List<CheckRunSelector.Field> fields,
		HttpServletRequest request
	) {
		Check check = getCheck(appId, checkId);
		DefaultRunContext checkContext = getContext(request);
		CheckRun checkRun = checkCheckRunner.runChecks(Arrays.asList(check), checkContext).get(0);
		return getCheckRunResponseEntity(checkRun, fields);
	}

	@RequestMapping(path = "/check", method = {RequestMethod.POST, RequestMethod.GET})
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
		DefaultRunContext checkContext = getContext(request);
		checkContext.putContext(monitorService.appPredicateKey(), checkFilter);
		checkContext.putContext(monitorService.predicateKey(), appFilter);
		CheckRun checkRun = monitorCheckRunner.runChecks(monitorCheck, checkContext).get(0);
		return getCheckRunResponseEntity(checkRun, fields);
	}

	@RequestMapping("/apps")
	public Collection<String> getAppNames() {
		return monitorService.getCheckNames();
	}

	@RequestMapping("/apps/{appId}")
	public Collection<String> getCheckNames(@PathVariable String appId) {
		return getApp(appId).getCheckNames();
	}

	private App getApp(String appId) {
		App app = monitorService.getApp(appId);
		if (app == null) {
			throw new ResourceNotFoundException("App " + appId + " not found");
		}
		return app;
	}

	private Check getCheck(String appId, String checkId) {
		App app = getApp(appId);
		Check check = app.getCheck(checkId);
		if (check == null) {
			throw new ResourceNotFoundException("Check " + checkId + " not found");
		}
		return check;
	}

	private DefaultRunContext getContext(HttpServletRequest request) {
		DefaultRunContext checkContext = new DefaultRunContext();
		checkContext.setShouldrun(shouldRun(request));
		return checkContext;
	}

	private boolean shouldRun(HttpServletRequest request) {
		return !"GET".equals(request.getMethod());
	}

	private ResponseEntity<CheckRun> getCheckRunResponseEntity(CheckRun checkRun, Collection<CheckRunSelector.Field> fields) {
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
