package org.towerhawk.serde.resolver;

import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionPoint;
import org.pf4j.PluginManager;
import org.towerhawk.monitor.active.Active;
import org.towerhawk.monitor.app.App;
import org.towerhawk.monitor.check.Check;
import org.towerhawk.monitor.check.cluster.Cluster;
import org.towerhawk.monitor.check.evaluation.Evaluator;
import org.towerhawk.monitor.check.evaluation.threshold.Threshold;
import org.towerhawk.monitor.check.evaluation.transform.Transform;
import org.towerhawk.monitor.check.execution.CheckExecutor;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.run.CheckRunner;
import org.towerhawk.monitor.schedule.ScheduleCollector;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * These could be retrieved through some complex logic and reflection
 * to only get interfaces that extend the ExtensionPoint interface,
 * but that is potentially very slow and startup should be as quick as
 * possible. This class exists to make it easy to retrieve the different
 * extensible types at runtime and also to serve as documentation of those
 * classes that are meant to be extendable by plugins.
 */
public class ExtensibleAPI {

	public static final List<Class<? extends ExtensionPoint>> CLASSES;

	static {
		List<Class<? extends ExtensionPoint>> classes = new ArrayList<>();
		classes.add(Active.class);
		classes.add(App.class);
		classes.add(Evaluator.class);
		classes.add(Transform.class);
		classes.add(Threshold.class);
		classes.add(CheckExecutor.class);
		classes.add(CheckRun.class);
		classes.add(Check.class);
		classes.add(CheckRunner.class);
		classes.add(ScheduleCollector.class);
		classes.add(Cluster.class);

		CLASSES = Collections.unmodifiableList(classes);
	}
}
