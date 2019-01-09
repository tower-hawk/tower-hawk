package org.towerhawk.controller;

import lombok.Getter;
import org.towerhawk.controller.exception.ResourceNotFoundException;
import org.towerhawk.monitor.MonitorService;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.context.DefaultRunContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Getter
@Named
public class ControllerMonitorServiceWrapper {

	private MonitorService monitorService;

	@Inject
	public ControllerMonitorServiceWrapper(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	public App getApp(String appId) {
		App app = monitorService.getApp(appId);
		if (app == null) {
			throw new ResourceNotFoundException("App " + appId + " not found");
		}
		return app;
	}

	public Check getCheck(String appId, String checkId) {
		App app = getApp(appId);
		Check check = app.getCheck(checkId);
		if (check == null) {
			throw new ResourceNotFoundException("Check " + checkId + " not found");
		}
		return check;
	}

	public DefaultRunContext getContext(HttpServletRequest request) {
		DefaultRunContext checkContext = new DefaultRunContext();
		checkContext.setShouldrun(shouldRun(request));
		return checkContext;
	}

	public boolean shouldRun(HttpServletRequest request) {
		return !"GET".equals(request.getMethod());
	}

}
