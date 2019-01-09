package org.towerhawk.monitor.check.run;

import org.pf4j.ExtensionPoint;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.serde.resolver.ExtensibleAPI;

import java.util.Collection;
import java.util.List;

public interface CheckRunner extends ExtensionPoint {

	List<CheckRun> runChecks(Collection<Check> checks, RunContext runContext);
}
