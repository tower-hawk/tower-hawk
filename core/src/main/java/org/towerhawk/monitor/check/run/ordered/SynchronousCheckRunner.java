package org.towerhawk.monitor.check.run.ordered;

import org.pf4j.Extension;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.serde.resolver.TowerhawkType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Extension
@TowerhawkType({"synchronous","sync"})
public class SynchronousCheckRunner implements CheckRunner {

	@Override
	public List<CheckRun> runChecks(Collection<Check> checks, RunContext runContext) {
		return checks.stream().map(c -> c.run(runContext)).collect(Collectors.toList());
	}
}
