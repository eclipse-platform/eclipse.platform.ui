package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.help.AppServer;

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
	public static boolean DEBUG_SHOW_IHANDLER = false;

	//The shared instance.
	private static UpdateManagerPlugin plugin;

	// web install
	private static String appServerHost =null;
	private static int appServerPort = 0;

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

		DEBUG = getBooleanDebugOption("org.eclipse.update.core/debug", false); //$NON-NLS-1$

		if (DEBUG) {
			DEBUG_SHOW_WARNINGS = getBooleanDebugOption("org.eclipse.update.core/debug/warning", false); //$NON-NLS-1$
			DEBUG_SHOW_PARSING = getBooleanDebugOption("org.eclipse.update.core/debug/parsing", false); //$NON-NLS-1$
			DEBUG_SHOW_INSTALL = getBooleanDebugOption("org.eclipse.update.core/debug/install", false); //$NON-NLS-1$
			DEBUG_SHOW_CONFIGURATION = getBooleanDebugOption("org.eclipse.update.core/debug/configuration", false); //$NON-NLS-1$
			DEBUG_SHOW_TYPE = getBooleanDebugOption("org.eclipse.update.core/debug/type", false); //$NON-NLS-1$
			DEBUG_SHOW_WEB = getBooleanDebugOption("org.eclipse.update.core/debug/web", false); //$NON-NLS-1$
			DEBUG_SHOW_IHANDLER = getBooleanDebugOption("org.eclipse.update.core/debug/installhandler", false); //$NON-NLS-1$
		}

//		startupWebInstallHandler();
	}

	private void startupWebInstallHandler() throws CoreException {
		
		// configure web install handler
		if (!AppServer.add("org.eclipse.update", "org.eclipse.update.webapp", "")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (DEBUG_SHOW_WEB)
				debug("Unable to configure web install handler"); //$NON-NLS-1$
			return;
		}

		appServerHost = AppServer.getHost();
		appServerPort = AppServer.getPort();
		if (DEBUG_SHOW_WEB)
			debug("Web install handler configured on " + appServerHost + ":" + appServerPort); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private boolean getBooleanDebugOption(String flag, boolean dflt) {
		String result = Platform.getDebugOption(flag);
		if (result == null)
			return dflt;
		else
			return result.trim().equalsIgnoreCase("true"); //$NON-NLS-1$
	}

	/**
	 * dumps a String in the trace
	 */
	public void debug(String s) {
		System.out.println(toString() + "^" + Integer.toHexString(Thread.currentThread().hashCode()) + " " + s); //$NON-NLS-1$ //$NON-NLS-2$
	}
}