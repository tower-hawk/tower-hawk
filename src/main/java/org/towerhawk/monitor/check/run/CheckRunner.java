package org.towerhawk.monitor.check.run;

import org.towerhawk.monitor.check.Check;

import java.util.Collection;
import java.util.List;

public interface CheckRunner {

	List<CheckRun> runChecks(Collection<Check> checks);
}
