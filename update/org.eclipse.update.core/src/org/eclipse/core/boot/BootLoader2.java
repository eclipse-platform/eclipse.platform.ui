package org.eclipse.core.boot;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.internal.boot.InternalBootLoader2;

public class BootLoader2 {
	
	public static IPlatformConfiguration getPlatformConfiguration() {
		return InternalBootLoader2.getPlatformConfiguration();
	}

}

