package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.help.AppServer;
import org.eclipse.update.core.Utilities;

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
	public static boolean DEBUG_SHOW_RECONCILER = false;

	//The shared instance.
	private static UpdateManagerPlugin plugin;
	private static SimpleDateFormat formatter = new SimpleDateFormat ("mm:ss:SSS");


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

		Policy.localize("org.eclipse.update.internal.core.messages"); //$NON-NLS-1$
		DEBUG = getBooleanDebugOption("org.eclipse.update.core/debug", false); //$NON-NLS-1$

		if (DEBUG) {
			DEBUG_SHOW_WARNINGS = getBooleanDebugOption("org.eclipse.update.core/debug/warning", false); //$NON-NLS-1$
			DEBUG_SHOW_PARSING = getBooleanDebugOption("org.eclipse.update.core/debug/parsing", false); //$NON-NLS-1$
			DEBUG_SHOW_INSTALL = getBooleanDebugOption("org.eclipse.update.core/debug/install", false); //$NON-NLS-1$
			DEBUG_SHOW_CONFIGURATION = getBooleanDebugOption("org.eclipse.update.core/debug/configuration", false); //$NON-NLS-1$
			DEBUG_SHOW_TYPE = getBooleanDebugOption("org.eclipse.update.core/debug/type", false); //$NON-NLS-1$
			DEBUG_SHOW_WEB = getBooleanDebugOption("org.eclipse.update.core/debug/web", false); //$NON-NLS-1$
			DEBUG_SHOW_IHANDLER = getBooleanDebugOption("org.eclipse.update.core/debug/installhandler", false); //$NON-NLS-1$
			DEBUG_SHOW_RECONCILER = getBooleanDebugOption("org.eclipse.update.core/debug/reconciler", false); //$NON-NLS-1$
		}
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
	public static void debug(String s) {
		Date d = new Date();
		String dateString = formatter.format(d);
		StringBuffer msg = new StringBuffer();
		msg.append(getPlugin().toString());
		msg.append("^");
		msg.append(Integer.toHexString(Thread.currentThread().hashCode()));
		msg.append("@");		
		msg.append(dateString);		
		msg.append(" ");
		msg.append(s);
		System.out.println(msg.toString());
	}
	
	/**
	 * Dumps a String in the log if WARNING is set to true
	 */
	public static void warn(String s) {
		if (DEBUG && DEBUG_SHOW_WARNINGS)
			debug(s); 
	}

	/**
	 * Dumps an exception in the log if WARNING is set to true
	 * 
	 * @param s log string
	 * @param e exception to be logged
	 * @since 2.0
	 */
	public static void warn(String s, Throwable e) {
		if (DEBUG && DEBUG_SHOW_WARNINGS){
			if (s!=null){
				s="Install/Update WARNING:"+s;
			}
			log(s,e);
		}
	} 
			
	/**
	 * Logs a status
	 */
	public static void log(IStatus status){
		UpdateManagerPlugin.getPlugin().getLog().log(status);		
	}
	
	/**
	 * Logs an error
	 */
	public static void log(Throwable e){		
		log("",e);
	}	
	
	/**
	 * Logs a string and an  error
	 */
	public static void log(String msg, Throwable e){
		IStatus status = null;
		if (e instanceof CoreException) 
			status = ((CoreException)e).getStatus();
		else 
			status = Utilities.newCoreException(msg,e).getStatus();		
		if (status!=null)
			log(status);
	}		
}