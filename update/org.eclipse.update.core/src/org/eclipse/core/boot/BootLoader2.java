package org.eclipse.core.boot;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.boot.PlatformConfiguration;

public class BootLoader2 {
	
	private BootLoader2() {}
	public static IPlatformConfiguration getPlatformConfiguration() {
		return new PlatformConfiguration();
	}

}

