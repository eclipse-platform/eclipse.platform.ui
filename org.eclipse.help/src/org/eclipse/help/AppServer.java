/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.HelpPlugin;

/**
 * Singleton class for obtaining the platform app server
 */
public class AppServer
{
	private final static String APP_SERVER_EXTENSION_ID =
		"org.eclipse.help.app-server";
	private static final String APP_SERVER_CLASS_ATTRIBUTE = "class";

	// singleton object
	private static IAppServer appServer;

	static {
		// Initializes the app server by getting an instance via 
		// app-server the extension point

		// get the app server extension from the system plugin registry	
		IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
		IExtensionPoint point =
			pluginRegistry.getExtensionPoint(APP_SERVER_EXTENSION_ID);
		if (point != null)
		{
			IExtension[] extensions = point.getExtensions();
			if (extensions.length != 0)
			{
				// There should only be one extension/config element so we just take the first
				IConfigurationElement[] elements = extensions[0].getConfigurationElements();
				if (elements.length != 0)
				{
					// Instantiate the app server
					try
					{
						appServer =
							(IAppServer) elements[0].createExecutableExtension(APP_SERVER_CLASS_ATTRIBUTE);
					}
					catch (CoreException e)
					{
						// may need to change this
						HelpPlugin.getDefault().getLog().log(e.getStatus());
					}
				}
			}
		}
	}

	/**
	 * Adds the specified webapp from a plugin.
	 * <p> It is assumed that webapp names are unique. It is suggested to
	 * create unique web app names by prefixing them with the plugin id.</p>
	 * @param webAppName the name of the web application (context name)
	 * @param plugin the plugin identifier of the webapp
	 * @param path the plugin relative path where the web app WAR or directory is located
	 * @return true if the webapp was added, false otherwise
	 */
	public static boolean add(String webAppName, String plugin, String path)
	{
		if (appServer == null)
			return false;
		return appServer.add(webAppName, plugin, path);
	}

	/**
	 * Removes the specified webapp
	 * @param webApp the name of the web app
	 * @param plugin the plugin containg the web app. Not used now.
	 */
	public static void remove(String webApp, String plugin)
	{
		if (appServer == null)
			return;
		appServer.remove(webApp, plugin);
	}

	/**
	 * Returns the host name or ip the app server runs on.
	 */
	public static String getHost()
	{
		if (appServer == null)
			return null;
		return appServer.getHost();
	}

	/**
	 * Returns the port number the app server listens on.
	 */
	public static int getPort()
	{
		if (appServer == null)
			return 0;
		return appServer.getPort();
	}

	/**
	 * Returns true if the app server is running
	 */
	public static boolean isRunning()
	{
		return appServer != null;
	}

	/**
	 * Sets the host and port for the app server. Must be called before adding a webapp,
	 * but the call is optional.
	 * @param host the host name or IP address. Pass null when any address on 
	 *         local machine is to be used.
	 * @param port the port number to be used. Pass 0 to let the system select
	 *         an available port.
	 */
	private void setAddress(String host, int port)
	{
		// leave the default, i.e. let the system choose the port
	}
}