package org.eclipse.update.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.internal.core.UpdateManagerPlugin;
/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */

/**
 * This is a base class for singleton handler of web-based install
 * requests. This class <b>must</b> be subclassed. A constructed instance
 * of handler is set by calling the setWebInstallHandler method.
 * 
 * @since 2.0
 */
public abstract class WebInstallHandler {
	
	private static WebInstallHandler handler = null;
	
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
			return "?eclipse="+url;
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
		if (host==null || port==0)
			return null;
		else
			return "http://" + host + ":" + port + "/install";
	}
		
	/**
	 * Sets the install handler for web requests. Once set, it cannot
	 * be reset.
	 * 
	 * @param handler handler instance. Must be a subclass of this class.
	 * @since 2.0
	 */
	public static void setWebInstallHandler(WebInstallHandler handler) {
		if (WebInstallHandler.handler == null)
			WebInstallHandler.handler = handler;
	}
		
	/**
	 * Returns the web install handler instance
	 * 
	 * @return web install handler
	 * @since 2.0
	 */
	public static WebInstallHandler getWebInstallHandler() {
		return WebInstallHandler.handler;
	}
		
	/**
	 * Abstract method called to perform the installation request.
	 * Subclasses provide an implementation.
	 * 
	 * @param sourceFeature feature to install
	 * @param targetSite target installation site
	 * @since 2.0
	 */
	protected abstract void performInstall(IFeature sourceFeature, ISite targetSite) throws CoreException;
}
