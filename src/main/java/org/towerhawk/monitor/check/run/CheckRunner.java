package org.towerhawk.monitor.check.run;

import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.context.RunContext;

import java.util.Collection;
import java.util.List;

public interface CheckRunner {

	List<CheckRun> runChecks(Collection<Check> checks, RunContext runContext);
}
