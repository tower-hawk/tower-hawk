package org.towerhawk.plugin.oshi.util;

import oshi.SystemInfo;

public class OSHIUtil {

	private static SystemInfo systemInfo = new SystemInfo();

	public static SystemInfo getSystemInfo() {
		return systemInfo;
	}

}
