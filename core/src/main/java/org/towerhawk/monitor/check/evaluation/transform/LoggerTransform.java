package org.towerhawk.monitor.check.evaluation.transform;

import lombok.Getter;
import lombok.Setter;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.function.Consumer;

@Getter
@Extension
@TowerhawkType("logger")
public class LoggerTransform implements Transform {

	@Setter
	private String level = "INFO";
	private String name = "logger";
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private transient Consumer<Object> loggingFunction = o -> log.info(o.toString());

	public void setName(String name) {
		this.name = name;
		log = LoggerFactory.getLogger(this.getClass().getCanonicalName() + "." + name);
	}

	public void setLevel(String level) {
		this.level = level;
		switch (level.toUpperCase()) {
			case "ERROR":
				loggingFunction = o -> log.error(o.toString());
				break;
			case "WARN":
				loggingFunction = o -> log.warn(o.toString());
				break;
			case "DEBUG":
				loggingFunction = o -> log.debug(o.toString());
				break;
			case "TRACE":
				loggingFunction = o -> log.trace(o.toString());
				break;
		}
	}

	@Override
	public Object transform(Object value) throws Exception {
		loggingFunction.accept(value);
		return value;
	}
}
