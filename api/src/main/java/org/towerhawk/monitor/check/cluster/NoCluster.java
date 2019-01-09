package org.towerhawk.monitor.check.cluster;

import lombok.Getter;
import org.pf4j.Extension;
import org.towerhawk.config.Config;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.serde.resolver.TowerhawkType;

@Getter
@Extension
@TowerhawkType({"default", "null", "noOp"})
public class NoCluster implements Cluster {

	protected boolean initialized = false;

	@Override
	public boolean isLeader() {
		return initialized;
	}

	@Override
	public boolean canExecute() {
		return initialized;
	}

	@Override
	public void init(Cluster previous, Check check, Config config) {
		initialized = true;
	}

	@Override
	public void close() throws Exception {
		initialized = false;
	}
}
