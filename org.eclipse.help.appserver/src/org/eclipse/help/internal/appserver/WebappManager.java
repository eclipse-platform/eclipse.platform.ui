/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.appserver;

import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.*;

/**
 * Singleton class to be called by clients to run a webapp.
 * 
 * @since 2.1 
 * @deprecated This internal interface is no longer used by the Eclipse Help 
 * system and should not be used by anyone else. It is likely to be removed 
 * in a future release. 
 * Use the HTTP service implementation provided by Equinox that is based 
 * on Jetty, see http://www.eclipse.org/equinox/server.
 */
public class WebappManager {
	private static boolean applicationsStarted = false;

	/**
	 * Private constructor, so no instances can be created.
	 * 
	 * @see java.lang.Object#Object()
	 */
	private WebappManager() {
	}

	/**
	 * Runs a webapp on the server. The webapp is defined in a plugin and the
	 * path is relative to the plugin directory.
	 * <p>
	 * It is assumed that webapp names are unique. It is suggested to create
	 * unique web app names by prefixing them with the plugin id.
	 * </p>
	 * 
	 * @param webappName
	 *            the name of the web app (also knowns as application context)
	 * @param pluginId
	 *            plugin that defines the webapp
	 * @param path
	 *            webapp relative path to the plugin directory
	 */
	public static void start(String webappName, String pluginId, IPath path)
			throws CoreException {

		IPath webappPath = getWebappPath(pluginId, path);

		// we get the server before constructing the class loader, so
		// class loader exposed by the server is available to the webapps.
		IWebappServer server = AppserverPlugin.getDefault().getAppServer();
		applicationsStarted = true;
		server.start(webappName, webappPath, new PluginClassLoaderWrapper(
				pluginId));
	}

	/**
	 * Stops the specified webapp.
	 */
	public static void stop(String webappName) throws CoreException {
		if (!applicationsStarted) {
			// do not obtain (start) appserver when no reason
			return;
		}
		AppserverPlugin.getDefault().getAppServer().stop(webappName);
	}

	/**
	 * Returns the port number the app server listens on.
	 * 
	 * @return integer port number, 0 if server not started
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
	 * 
	 * @return String representaion of host name of IP, null if server not
	 *         started yet
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
	 * @param path
	 *            webapp path relative to the plugin directory
	 * @return String absolute webapp path
	 */
	private static IPath getWebappPath(String pluginId, IPath path)
			throws CoreException {

		Bundle bundle = Platform.getBundle(pluginId);
		if (bundle == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					AppserverPlugin.PLUGIN_ID, IStatus.OK, NLS.bind(AppserverResources.Appserver_cannotFindPlugin, pluginId), null));
		}

		// Note: we just look for one webapp directory.
		//       If needed, may want to use the locale specific path.
		URL webappURL = FileLocator.find(bundle, path, null);
		if (webappURL == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					AppserverPlugin.PLUGIN_ID, IStatus.OK, NLS.bind(AppserverResources.Appserver_cannotFindPath, pluginId, path.toOSString()), null));
		}

		try {
			String webappLocation = FileLocator.toFileURL(
					FileLocator.resolve(webappURL)).getFile();
			return new Path(webappLocation);
		} catch (IOException ioe) {
			throw new CoreException(new Status(IStatus.ERROR,
					AppserverPlugin.PLUGIN_ID, IStatus.OK, NLS.bind(AppserverResources.Appserver_cannotResolvePath, pluginId, path.toOSString()), ioe));
		}
	}
}
