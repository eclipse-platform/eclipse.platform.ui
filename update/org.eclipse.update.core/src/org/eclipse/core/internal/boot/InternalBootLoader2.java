package org.eclipse.core.internal.boot;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.net.URL;

public class InternalBootLoader2 {
	
	private static PlatformConfiguration current = null;
	
	public static PlatformConfiguration getCurrentPlatformConfiguration() throws IOException {
		if (current == null) 
			current = new PlatformConfiguration();
		return current;
	}
	
	public static PlatformConfiguration getPlatformConfiguration(URL url) throws IOException {
		return new PlatformConfiguration(url);
	}

}

