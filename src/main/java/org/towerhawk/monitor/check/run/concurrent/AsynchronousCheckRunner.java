package org.towerhawk.monitor.check.run.concurrent;

import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.CheckContext;
import org.towerhawk.monitor.check.run.CheckRunAccumulator;
import org.towerhawk.monitor.check.run.CheckRunner;

import java.util.Collection;

public interface AsynchronousCheckRunner extends CheckRunner {

	CheckRunAccumulator runChecksAsync(Collection<Check> checks, CheckContext checkContext);
}
