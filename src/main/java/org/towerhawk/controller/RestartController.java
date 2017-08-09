package org.towerhawk.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.filter.CheckFilter;
import org.towerhawk.spring.config.Configuration;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class RestartController {

	private final ControllerMonitorServiceWrapper monitorServiceWrapper;
	private final Configuration configuration;

	@Inject
	public RestartController(ControllerMonitorServiceWrapper monitorServiceWrapper, Configuration configuration) {
		this.monitorServiceWrapper = monitorServiceWrapper;
		this.configuration = configuration;
	}

	@RequestMapping(path = "/app/restart", method = {RequestMethod.POST, RequestMethod.GET})
	public Map<String, Boolean> restartApps(
		@RequestParam(required = false) List<Integer> priority,
		@RequestParam(required = false) Integer priorityLte,
		@RequestParam(required = false) Integer priorityGte,
		@RequestParam(required = false) List<String> tags,
		@RequestParam(required = false) List<String> notTags,
		@RequestParam(required = false) List<String> type,
		@RequestParam(required = false) List<String> notType,
		@RequestParam(required = false) List<String> id,
		@RequestParam(required = false) List<String> notId,
		@RequestParam(defaultValue = "true") boolean restarting,
		HttpServletRequest request
	) {
		CheckFilter checkFilter = new CheckFilter(priority, priorityLte, priorityGte, tags, notTags, type, notType, id, notId);
		List<Check> checks = monitorServiceWrapper.getMonitorService().getChecks().values().stream().filter(checkFilter::filter).collect(Collectors.toList());
		if (monitorServiceWrapper.shouldRun(request)) {
			checks.forEach(c -> c.setRestarting(restarting));
		}
		return checks.stream().collect(Collectors.toMap(Check::getId, Check::isRestarting));
	}

	@RequestMapping(path = "/app/{appId}/restart", method = {RequestMethod.POST, RequestMethod.GET})
	public Boolean restartApp(
		@PathVariable String appId,
		@RequestParam(defaultValue = "true") boolean restarting,
		HttpServletRequest request
	) {
		App app = monitorServiceWrapper.getApp(appId);
		if (monitorServiceWrapper.shouldRun(request)) {
			app.setRestarting(restarting);
		}
		return app.isRestarting();
	}

	@RequestMapping(path = "/app/{appId}/{checkId}/restart", method = {RequestMethod.POST, RequestMethod.GET})
	public Boolean restartCheck(
		@PathVariable String appId,
		@PathVariable String checkId,
		@RequestParam(defaultValue = "true") boolean restarting,
		HttpServletRequest request
	) {
		Check check = monitorServiceWrapper.getCheck(appId, checkId);
		if (monitorServiceWrapper.shouldRun(request)) {
			check.setRestarting(restarting);
		}
		return check.isRestarting();
	}

	@RequestMapping(path = "/check/restart", method = {RequestMethod.POST, RequestMethod.GET})
	public Map<String, Map<String, Boolean>> restartChecks(
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
		@RequestParam(defaultValue = "true") boolean restarting,
		HttpServletRequest request
	) {
		CheckFilter checkFilter = new CheckFilter(priority, priorityLte, priorityGte, tags, notTags, type, notType, id, notId);
		CheckFilter appFilter = new CheckFilter(appPriority, appPriorityLte, appPriorityGte, appTags, appNotTags, appType, appNotType, appId, appNotId);
		List<Check> apps = monitorServiceWrapper.getMonitorService().getChecks().values().stream().filter(appFilter::filter).collect(Collectors.toList());
		Map<String, Map<String, Boolean>> returnMap = new HashMap<>();
		for (Check app : apps) {
			List<Check> checks = ((App) app).getChecks().values().stream().filter(checkFilter::filter).collect(Collectors.toList());
			if (monitorServiceWrapper.shouldRun(request)) {
				checks.forEach(c -> c.setRestarting(restarting));
			}
			Map<String, Boolean> checksRestarting = checks.stream().collect(Collectors.toMap(Check::getId, Check::isRestarting));
			if (!checksRestarting.isEmpty()) {
				returnMap.put(app.getId(), checksRestarting);
			}
		}
		return returnMap;
	}
}
