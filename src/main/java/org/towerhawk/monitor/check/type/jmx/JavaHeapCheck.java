package org.towerhawk.monitor.check.type.jmx;

import org.towerhawk.monitor.check.type.JmxCheck;
import org.towerhawk.serde.resolver.CheckType;

@CheckType("javaheap")
public class JavaHeapCheck extends JmxCheck {

	public JavaHeapCheck() {
		setMbean("java.lang:type=Memory");
		setAttribute("HeapMemoryUsage");
		setPath("used");
		setBasePath("max");
	}
}
