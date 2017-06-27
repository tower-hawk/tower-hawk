package org.towerhawk;

import org.springframework.web.bind.annotation.RestController;
import org.towerhawk.check.CheckService;

import javax.inject.Inject;

@RestController
public class Controller {

	@Inject
	private CheckService checkService;

	//	@RequestMapping(path="/getCheck/reload", method=RequestMethod.GET)
	//	public boolean reload(@RequestParam(value="name", defaultValue="World") String name) {
	//		return checkService.refreshDefinitions();
	//	}
	//
	//	@RequestMapping(path="/healthCheck/{path1}/{path2}", method=RequestMethod.GET)
	//	public CheckService greetingPath(@RequestParam(value="name", defaultValue="World") String name,
	//																	 @PathVariable(value="path1") String path,
	//																	 @PathVariable(value="path2") String path2) {
	//		//return new HealthCheck(counter.incrementAndGet(), String.format(template, name), path, null);
	//		return checkService;
	//	}
	//
	//	@RequestMapping(value = "/getCheck/**", method=RequestMethod.GET)
	//	public CheckService getFoo(final HttpServletRequest request,
	//														 @RequestParam String name) {
	//
	//		String path = (String) request.getAttribute(
	//			HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
	//		String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
	//
	//		AntPathMatcher apm = new AntPathMatcher();
	//		String finalPath = apm.extractPathWithinPattern(bestMatchPattern, path);
	//		// on "/properties/foo/bar", finalPath contains "foo/bar"
	//		//return new HealthCheck(counter.getAndIncrement(), name, finalPath, checkService.getReturnVal());
	//		return checkService;
	//	}
}
