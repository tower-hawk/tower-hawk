package org.towerhawk.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.towerhawk.check.Check;
import org.towerhawk.check.CheckService;
import org.towerhawk.check.app.App;
import org.towerhawk.check.run.CheckRun;

import javax.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/check")
public class CheckRunnerController {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final CheckService checkService;

	@Inject
	public CheckRunnerController(CheckService checkService) {
		this.checkService = checkService;
	}

	@RequestMapping(path = "/app", method = {RequestMethod.POST})
	public ResponseEntity<String> runAllApps() {
		CheckRun checkRun = checkService.run();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, false);
		mapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, true);
		String returnVal = "";
		try {
			returnVal = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(checkRun);
		} catch (JsonProcessingException e) {
			log.error("Error writing object to string", e);
		}
		return new ResponseEntity<String>(returnVal, HttpStatus.valueOf(200));
	}

	@RequestMapping(path = "/app/{appId}")
	public Map<String, CheckRun> getRecentRunsByApp(@PathVariable(value = "appId") String appId) {
		App app = checkService.getApp(appId);
		if (app == null) {
			throw new IllegalArgumentException(String.format("App with id %s not found", appId));
		}
		Map<String, Check> checks = app.getChecks();
		return checks.entrySet().stream().collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue().getLastCheckRun()));
	}

	@RequestMapping(path = "/app/{appId}", method = {RequestMethod.POST})
	public String runByApp(@PathVariable(value = "appId") String appId) {
		CheckRun checkRun = checkService.getApp(appId).run();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, false);
		mapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, true);
		mapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
		String returnVal = "";
		try {
			returnVal = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(checkRun);
		} catch (JsonProcessingException e) {
			log.error("Error writing object to string", e);
		}
		return returnVal;
	}
}
