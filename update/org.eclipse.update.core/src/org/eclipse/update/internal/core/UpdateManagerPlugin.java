package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.help.IAppServer;

/**
 * The main plugin class to be used in the desktop.
 */
public class UpdateManagerPlugin extends Plugin {

	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_SHOW_INSTALL = false;
	public static boolean DEBUG_SHOW_PARSING = false;
	public static boolean DEBUG_SHOW_WARNINGS = false;
	public static boolean DEBUG_SHOW_CONFIGURATION = false;
	public static boolean DEBUG_SHOW_TYPE = false;
	public static boolean DEBUG_SHOW_WEB = false;

	//The shared instance.
	private static UpdateManagerPlugin plugin;

	// web install
	private static IAppServer appServer = null;
	private static String appServerHost = "127.0.0.1";
	private static int appServerPort = 1333;

	/**
	 * The constructor.
	 */
	public UpdateManagerPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static UpdateManagerPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Returns the host identifier for the web app server
	 */
	public static String getWebAppServerHost() {
		return appServerHost;
	}

	/**
	 * Returns the port identifier for the web app server
	 */
	public static int getWebAppServerPort() {
		return appServerPort;
	}

	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();

		DEBUG = getBooleanDebugOption("org.eclipse.update.core/debug", false);

		if (DEBUG) {
			DEBUG_SHOW_WARNINGS = getBooleanDebugOption("org.eclipse.update.core/debug/warning", false);
			DEBUG_SHOW_PARSING = getBooleanDebugOption("org.eclipse.update.core/debug/parsing", false);
			DEBUG_SHOW_INSTALL = getBooleanDebugOption("org.eclipse.update.core/debug/install", false);
			DEBUG_SHOW_CONFIGURATION = getBooleanDebugOption("org.eclipse.update.core/debug/configuration", false);
			DEBUG_SHOW_TYPE = getBooleanDebugOption("org.eclipse.update.core/debug/type", false);
			DEBUG_SHOW_WEB = getBooleanDebugOption("org.eclipse.update.core/debug/web", false);
		}

		startupWebInstallHandler();
	}

	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		if (appServer != null) {
			boolean stopOK = appServer.stop();
			if (DEBUG_SHOW_WEB) {
				if (stopOK)
					debug("Web app server stopped");
				else
					debug("Failed to stop web app server");
			}
		}

	}

	private void startupWebInstallHandler() throws CoreException {
		// get handle for the web app server
		IAppServer localAppServer = getWebAppServer();
		if (localAppServer == null) {
			if (DEBUG_SHOW_WEB)
				debug("Unable to obtain web app server");
			return;
		}

		// configure web install handler 

		// start a listener port for the web install handler
		if (!localAppServer.start()) {
			localAppServer = null;
			if (DEBUG_SHOW_WEB)
				debug("Failed to start web app server");
			return;
		}

		appServerHost = localAppServer.getHost();
		appServerPort = localAppServer.getPort();
		if (DEBUG_SHOW_WEB)
			debug("Web app server started on " + appServerHost + ":" + appServerPort);
	}

	public static IAppServer getWebAppServer() throws CoreException {
		// FIXME: this code needs to be exposed as an API on some core class
		if (appServer == null) {
			// get the app server extension from the system plugin registry	
			IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
			IExtensionPoint point = pluginRegistry.getExtensionPoint("org.eclipse.help.app-server");
			if (point == null)
				return null;
			IExtension[] extensions = point.getExtensions();
			if (extensions.length == 0)
				return null;
			// There should only be one extension/config element so we just take the first
			IConfigurationElement[] elements = extensions[0].getConfigurationElements();
			if (elements.length == 0)
				return null;
			// Instantiate the app server
			appServer = (IAppServer) elements[0].createExecutableExtension("class");
		}
		
		return appServer;
	}

	private boolean getBooleanDebugOption(String flag, boolean dflt) {
		String result = Platform.getDebugOption(flag);
		if (result == null)
			return dflt;
		else
			return result.trim().equalsIgnoreCase("true");
	}

	/**
	 * dumps a String in the trace
	 */
	public void debug(String s) {
		System.out.println(toString() + "^" + Integer.toHexString(Thread.currentThread().hashCode()) + " " + s);
	}
}