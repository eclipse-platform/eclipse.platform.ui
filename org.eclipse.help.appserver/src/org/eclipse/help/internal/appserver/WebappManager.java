/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.appserver;


import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.*;

/**
 * Singleton class to be called by clients to run a webapp.
 * @since 2.1
 */
public class WebappManager {

	/**
	 * Private constructor, so no instances can be created.
	 * @see java.lang.Object#Object()
	 */
	private WebappManager() {
	}

	/**
	 * Runs a webapp on the server. The webapp is defined in a plugin and the path is relative
	 * to the plugin directory.
	 * <p> It is assumed that webapp names are unique. It is suggested to create
	 * unique web app names by prefixing them with the plugin id.</p>
	 * 
	 * @param webappName the name of the web app (also knowns as application context)
	 * @param pluginId plugin that defines the webapp
	 * @param path webapp relative path to the plugin directory
	 * @ return  true if the webapp was successfully started, false otherwise
	 */
	public static void start(String webappName, String pluginId, IPath path)
		throws CoreException {

		IPath webappPath = getWebappPath(pluginId, path);

		// we get the server before constructing the class loader, so
		// class loader exposed by the server is available to the webapps.
		IWebappServer server = AppserverPlugin.getDefault().getAppServer();
		server.start(webappName, webappPath, new PluginClassLoaderWrapper(pluginId));
	}

	/**
	 * Stops the specified webapp.
	 */
	public static void stop(String webappName) throws CoreException {
		AppserverPlugin.getDefault().getAppServer().stop(webappName);
	}

	/**
	 * Returns the port number the app server listens on.
	 * @return integer port number,
	 *  0 if server not started
	 */
	public static int getPort() {
		try {
			return AppserverPlugin.getDefault().getAppServer().getPort();
		} catch (CoreException e) {
			return 0;
		}
	}

	/**
	 * Returns the host name or ip the app server runs on.
	 * @return String representaion of host name of IP,
	 *  null if server not started yet
	 */
	public static String getHost() {
		try {
			return AppserverPlugin.getDefault().getAppServer().getHost();
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * @param pluginId
	 * @param path webapp path relative to the plugin directory
	 * @return String absolute webapp path
	 */
	private static IPath getWebappPath(String pluginId, IPath path)
		throws CoreException {

		IPluginDescriptor descriptor =
			Platform.getPluginRegistry().getPluginDescriptor(pluginId);
		if (descriptor == null) {
			throw new CoreException(
				new Status(
					IStatus.ERROR,
					AppserverPlugin.getID(),
					IStatus.OK,
					AppserverResources.getString(
						"Appserver.cannotFindPlugin",
						pluginId),
					null));
		}

		// Note: we just look for one webapp directory.
		//       If needed, may want to use the locale specific path.
		URL webappURL = descriptor.find(path);
		if (webappURL == null) {
			throw new CoreException(
				new Status(
					IStatus.ERROR,
					AppserverPlugin.getID(),
					IStatus.OK,
					AppserverResources.getString(
						"Appserver.cannotResolvePlugin",
						pluginId),
					null));
		}

		try {
			String webappLocation = Platform.resolve(webappURL).getFile();
			return new Path(webappLocation);
		} catch (IOException ioe) {
			throw new CoreException(
				new Status(
					IStatus.ERROR,
					AppserverPlugin.getID(),
					IStatus.OK,
					AppserverResources.getString(
						"Appserver.cannotResolvePlugin",
						pluginId),
					ioe));
		}
	}

}
