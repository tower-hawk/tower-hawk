package org.towerhawk.monitor.check.evaluation.transform;

import lombok.NonNull;
import org.pf4j.ExtensionPoint;

public interface Transform<T> extends ExtensionPoint {
	T transform(@NonNull Object value) throws Exception;
}
