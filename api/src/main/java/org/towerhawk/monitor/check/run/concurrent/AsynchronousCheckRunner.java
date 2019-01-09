package org.towerhawk.monitor.check.run.concurrent;

import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.CheckRunAccumulator;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.check.run.context.RunContext;
import org.towerhawk.serde.resolver.ExtensibleAPI;

import java.util.Collection;

public interface AsynchronousCheckRunner extends CheckRunner {

	CheckRunAccumulator runChecksAsync(Collection<Check> checks, RunContext runContext);
}
