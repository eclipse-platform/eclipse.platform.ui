package org.eclipse.core.boot;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.internal.boot.InternalBootLoader2;

public class BootLoader2 {
	
	/**
	 * Return the current platform configuration
	 * 
	 * @return platform configuration used in current instance of platform
	 */	
	public static IPlatformConfiguration getCurrentPlatformConfiguration() throws IOException {
		return InternalBootLoader2.getCurrentPlatformConfiguration();
	}
	
	/**
	 * Return a platform configuration object, optionally initialized with previously saved
	 * configuration information
	 * 
	 * @param url location of previously save configuration information. If <code>null</code>
	 * is specified, an empty configuration object is returned
	 * @return platform configuration used in current instance of platform
	 */	
	public static IPlatformConfiguration getPlatformConfiguration(URL url) throws IOException {
		return InternalBootLoader2.getPlatformConfiguration(url);
	}

}

