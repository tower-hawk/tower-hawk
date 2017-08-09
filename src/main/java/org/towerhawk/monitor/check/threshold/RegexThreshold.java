package org.towerhawk.monitor.check.threshold;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.serde.resolver.ThresholdType;

import java.util.regex.Pattern;

@Slf4j
@ThresholdType("regex")
public class RegexThreshold implements Threshold {
	protected Pattern successRegex;
	protected Pattern warningRegex;
	protected Pattern criticalRegex;
	protected boolean addContext = true;
	protected boolean setMessage = true;

	@Override
	public boolean isAddContext() {
		return addContext;
	}

	@Override
	public boolean isSetMessage() {
		return setMessage;
	}

	@JsonCreator
	public RegexThreshold(
		@JsonProperty("success") String successRegex,
		@JsonProperty("warning") String warningRegex,
		@JsonProperty("critical") String criticalRegex,
		@JsonProperty("addContext") Boolean addContext,
		@JsonProperty("setMessage") Boolean setMessage
	) {
		this.successRegex = Pattern.compile(successRegex);
		this.warningRegex = Pattern.compile(warningRegex);
		this.criticalRegex = Pattern.compile(criticalRegex);

		if ( addContext != null ) {
			this.addContext = addContext;
		}

		if ( setMessage != null ) {
			this.setMessage = setMessage;
		}
	}

	@Override
	public CheckRun.Status evaluate(CheckRun.Builder builder, double value) {
		return evaluate(builder, String.valueOf(value));
	}

	@Override
	public CheckRun.Status evaluate(CheckRun.Builder builder, String value) {
		if ( criticalRegex != null && criticalRegex.matcher(value).find() ) {
			addContextAndMessage(builder, "criticalThreshold", value);
			return CheckRun.Status.CRITICAL;
		} else if ( warningRegex != null && warningRegex.matcher(value).find() ) {
			addContextAndMessage(builder, "warningThreshold", value);
			return CheckRun.Status.WARNING;
		} else if ( successRegex != null && successRegex.matcher(value).find() ) {
			return CheckRun.Status.SUCCEEDED;
		}

		return CheckRun.Status.UNKNOWN;
	}

	protected void addContextAndMessage(CheckRun.Builder builder, String keyName, String value) {
		String message = String.format("Found %s in value", value);
		if ( isAddContext() ) {
			builder.addContext(keyName, message);
		}
		if ( isSetMessage() ) {
			builder.message(message);
		}
	}

	@Override
	public CheckRun.Status evaluate(CheckRun.Builder builder, Object value) {
		return evaluate(builder, String.valueOf(value));
	}
}
