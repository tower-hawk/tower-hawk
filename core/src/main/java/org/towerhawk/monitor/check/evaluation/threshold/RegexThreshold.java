package org.towerhawk.monitor.check.evaluation.threshold;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.run.CheckRun;

import java.util.regex.Pattern;

@Slf4j
public class RegexThreshold implements Threshold {
	protected Pattern successRegex;
	protected Pattern warningRegex;
	protected Pattern criticalRegex;

	@JsonCreator
	public RegexThreshold(
			@JsonProperty("success") String successRegex,
			@JsonProperty("warning") String warningRegex,
			@JsonProperty("critical") String criticalRegex
	) {
		this.successRegex = Pattern.compile(successRegex);
		this.warningRegex = Pattern.compile(warningRegex);
		this.criticalRegex = Pattern.compile(criticalRegex);
	}

	@Override
	public void evaluate(CheckRun.Builder builder, String key, Object val, boolean setMessage, boolean addContext) throws Exception {
		String value = val.toString();
		if (criticalRegex != null && criticalRegex.matcher(value).find()) {
			addContextAndMessage(builder, "criticalThreshold", value, addContext, setMessage);
			builder.critical();
		} else if (warningRegex != null && warningRegex.matcher(value).find()) {
			addContextAndMessage(builder, "warningThreshold", value, addContext, setMessage);
			builder.warning();
		} else if (successRegex == null || successRegex.matcher(value).find()) {
			builder.succeeded();
		} else {
			builder.unknown();
		}
	}

	protected void addContextAndMessage(CheckRun.Builder builder, String keyName, String value, boolean addContext, boolean setMessage) {
		String message = String.format("Found %s in value", value);
		if (addContext) {
			builder.addContext(keyName, message);
		}
		if (setMessage) {
			builder.message(message);
		}
	}
}
