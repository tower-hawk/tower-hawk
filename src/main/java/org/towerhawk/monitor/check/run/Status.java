package org.towerhawk.monitor.check.run;

/**
 * An enum representing the status of a ${@link CheckRun}. They have an ordering
 * so that the most critical runs get returned first.
 */
public enum Status {
	CRITICAL(0),
	WARNING(1),
	UNKNOWN(2),
	SUCCEEDED(3);

	int ordinal;

	Status(int ordinal) {
		this.ordinal = ordinal;
	}

	int getOrdinal() {
		return ordinal;
	}
}
