package org.towerhawk.monitor.descriptors;

import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;

public interface Initializable<T> {
	void init(T previous, Check check, Config config);
}
