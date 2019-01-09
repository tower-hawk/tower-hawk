package org.towerhawk.monitor.active;

import org.pf4j.ExtensionPoint;

public interface Active extends ExtensionPoint {

	boolean isActive();
}
