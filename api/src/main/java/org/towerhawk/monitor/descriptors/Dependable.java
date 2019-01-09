package org.towerhawk.monitor.descriptors;

import java.util.Collections;
import java.util.List;

import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.run.Status;

public interface Dependable {

		/**
	 * Returns any checks that need to be run before this check. If all checks listed here
	 * are successful then this check can be run
	 *
	 * @return a list of checks if there are dependencies. A null or empty collection will be
	 * ignored.
	 */
	default List<Check> runAfterSuccess() {
		return Collections.emptyList();
	}

	/**
	 * Returns any checks that need to fail before this check should run. If any of the checks
	 * have a {@link Status} that != SUCCEEDED then
	 * this check can be run
	 *
	 * @return a list of checks if there are dependences that need to fail. A null or empty
	 * return value will be ignored.
	 */
	default List<Check> runAfterFailure() {
		return Collections.emptyList();
	}
}
