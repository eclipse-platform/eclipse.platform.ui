package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.update.internal.core.UpdateManagerPlugin;

/**
 * This is a utility class used find information about the update
 * web application (handling web-triggered requests)
 * 
 * @since 2.0
 */
public abstract class WebInstallHandler {

	/**
	 * Returns the host identifier for the web application server
	 * listener for install requests
	 * 
	 * @return host identifier, or <code>null</code> if web application
	 * server is not available
	 * @since 2.0
	 */
	public static String getWebAppServerHost() {
		return UpdateManagerPlugin.getWebAppServerHost();
	}

	/**
	 * Returns the port identifier for the web application server
	 * listener for install requests
	 * 
	 * @return port identifier, or <code>0</code> if web application
	 * server is not available
	 * @since 2.0
	 */
	public static int getWebAppServerPort() {
		return UpdateManagerPlugin.getWebAppServerPort();
	}

	/**
	 * Returns the callback string to be passed to the web page
	 * containing install triggers.
	 * 
	 * @return callback string
	 * @since 2.0
	 */
	public static String getCallbackString() {
		String url = getCallbackURLAsString();
		if (url == null)
			return null;
		else
			return "?eclipse=" + url; //$NON-NLS-1$
	}

	/**
	 * Returns the callback URL (as string) to be passed to the web page
	 * containing install triggers.
	 * 
	 * @return callback URL as string
	 * @since 2.0
	 */
	public static String getCallbackURLAsString() {
		String host = getWebAppServerHost();
		int port = getWebAppServerPort();
		if (host == null || port == 0)
			return null;
		else
			return "http://" + host + ":" + port + "/install";
		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

}