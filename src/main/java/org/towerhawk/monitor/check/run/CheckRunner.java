package org.towerhawk.monitor.check.run;

import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.CheckContext;

import java.util.Collection;
import java.util.List;

public interface CheckRunner {

	List<CheckRun> runChecks(Collection<Check> checks, CheckContext checkContext);
}
