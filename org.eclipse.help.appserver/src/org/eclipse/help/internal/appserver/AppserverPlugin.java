/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.appserver;
import org.eclipse.core.runtime.*;
import org.osgi.framework.*;
/**
 */
public class AppserverPlugin extends Plugin {
	public final static String PLUGIN_ID = "org.eclipse.help.appserver"; //$NON-NLS-1$
	public final static String HOST_KEY = "host"; //$NON-NLS-1$
	public final static String PORT_KEY = "port"; //$NON-NLS-1$
	private final static String APP_SERVER_EXTENSION_ID = PLUGIN_ID + ".server"; //$NON-NLS-1$
	private static final String APP_SERVER_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static final String APP_SERVER_DEFAULT_ATTRIBUTE = "default"; //$NON-NLS-1$
	// singleton object
	private static AppserverPlugin plugin;
	private static BundleContext bundleContext;
	private IWebappServer appServer;
	private String contributingServerPlugin;
	private boolean initialized = false;
	private String hostAddress;
	private int port;
	/**
	 */
	public static AppserverPlugin getDefault() {
		return plugin;
	}
	/**
	 * Returns the instance of WebappServer.
	 */
	public synchronized IWebappServer getAppServer() throws CoreException {
		if (appServer == null) {
			createWebappServer();
			startWebappServer();
		}
		return appServer;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (appServer != null) {
			appServer.stop();
		}
		plugin = null;
		bundleContext = null;
		super.stop(context);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;
	}
	/**
	 * Returns the plugin ID that contributes the server implementation
	 * 
	 * @return String
	 */
	public String getContributingServerPlugin() {
		return contributingServerPlugin;
	}
	private void createWebappServer() throws CoreException {
		initialized = true;
		// Initializes the app server by getting an instance via
		// app-server the extension point
		// get the app server extension from the system plugin registry
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(APP_SERVER_EXTENSION_ID);
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			if (extensions.length != 0) {
				// We need to pick up the non-default configuration
				IConfigurationElement[] elements = extensions[0]
						.getConfigurationElements();
				if (elements.length == 0)
					return;
				IConfigurationElement serverElement = null;
				for (int i = 0; i < elements.length; i++) {
					String defaultValue = elements[i]
							.getAttribute(APP_SERVER_DEFAULT_ATTRIBUTE);
					if (defaultValue == null || defaultValue.equals("false")) { //$NON-NLS-1$
						serverElement = elements[i];
						break;
					}
				}
				// if all the servers are default, then pick the first one
				if (serverElement == null)
					serverElement = elements[0];
				// Instantiate the app server
				try {
					appServer = (IWebappServer) serverElement
							.createExecutableExtension(APP_SERVER_CLASS_ATTRIBUTE);
					contributingServerPlugin = serverElement
							.getNamespace();
				} catch (CoreException e) {
					getLog().log(e.getStatus());
					throw e;
				}
			}
		}
	}
	private void startWebappServer() throws CoreException {
		// Initialize host and port from preferences
		hostAddress = getPluginPreferences().getString(HOST_KEY);
		if ("".equals(hostAddress)) { //$NON-NLS-1$
			hostAddress = null;
		}
		port = getPluginPreferences().getInt(PORT_KEY);
		// apply host and port overrides passed as command line arguments
		try {
			String hostCommandLineOverride = System.getProperty("server_host"); //$NON-NLS-1$
			if (hostCommandLineOverride != null
					&& hostCommandLineOverride.trim().length() > 0) {
				hostAddress = hostCommandLineOverride;
			}
		} catch (Exception e) {
		}
		try {
			String portCommandLineOverride = System.getProperty("server_port"); //$NON-NLS-1$
			if (portCommandLineOverride != null
					&& portCommandLineOverride.trim().length() > 0) {
				port = Integer.parseInt(portCommandLineOverride);
			}
		} catch (Exception e) {
		}
		if (appServer == null)
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					IStatus.OK,
					AppserverResources.getString("Appserver.start"), null)); //$NON-NLS-1$
		appServer.start(port, hostAddress);
	}
}
