package org.eclipse.core.internal.boot;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class InternalBootLoader2 {
	
	public static PlatformConfiguration getPlatformConfiguration() {
		int status = PlatformConfiguration.startup();
		return PlatformConfiguration.getCurrent();
	}

}

