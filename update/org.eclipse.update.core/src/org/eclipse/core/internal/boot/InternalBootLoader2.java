package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

public class InternalBootLoader2 {
	
	private static PlatformConfiguration current = null;
	
	public static PlatformConfiguration getPlatformConfiguration() {
		
		int status;
		
		if (current == null) {
			current = new PlatformConfiguration();
			try {
				current.save();
			} catch (IOException e) {
			}
		}
		return current;
	}

}

