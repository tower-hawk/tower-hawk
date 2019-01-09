package org.towerhawk.scripting;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Setter
@Getter
@Slf4j
public abstract class AbstractScriptEvaluator {

	protected String name;
	protected String function = "evaluate";
	protected String script;
	protected File file;

	public AbstractScriptEvaluator(String name, String function, String script, String file) {
		setName(name);
		setFunction(function);
		setScript(script);
		setFile(file);

		if ((script == null || script.isEmpty()) && file != null && !file.isEmpty()) {
			try {
				if (this.file.canExecute() && this.file.canRead()) {
					this.script = new String(Files.readAllBytes(this.file.toPath()), StandardCharsets.UTF_8);
				}
			} catch (Exception e) {
				log.error("Unable to read file {} for {}", file, name, e);
			}
		}

		if (this.script == null || this.script.isEmpty()) {
			throw new IllegalArgumentException("Either script or file must be set for '" + getName() + "' and not have empty contents. The towerhawk user must have read and execute permissions on the file.");
		}
	}

	public void setFile(String file) {
		if (file != null && !file.isEmpty()) {
			this.file = new File(file);
		}
	}

	public abstract Object invoke(Object... args) throws Exception;
}