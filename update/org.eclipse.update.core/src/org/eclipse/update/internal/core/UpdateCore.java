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
package org.eclipse.update.internal.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.update.configuration.IInstallConfiguration;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IImport;
import org.eclipse.update.core.JarContentReference;
import org.eclipse.update.core.SiteManager;
import org.eclipse.update.core.Utilities;


/**
 * The main plugin class to be used in the desktop.
 */
public class UpdateCore extends Plugin {

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

	// preference keys
	private static final String PREFIX = "org.eclipse.update.core";
	public static final String P_HISTORY_SIZE = PREFIX + ".historySize";
	public static final String P_CHECK_SIGNATURE = PREFIX + ".checkSignature";
	public static final String P_UPDATE_VERSIONS = PREFIX + ".updateVersions";
	public static final String EQUIVALENT_VALUE = "equivalent";
	public static final String COMPATIBLE_VALUE = "compatible";
	
	//The shared instance.
	private static UpdateCore plugin;

	//log
	private static UpdateManagerLogWriter log;
	private static final String LOG_FILE=".install-log";
	
	//Connection manager
	private ConnectionThreadManager connectionManager;

	public static String HTTP_PROXY_HOST = "org.eclipse.update.core.proxy.host";
	public static String HTTP_PROXY_PORT = "org.eclipse.update.core.proxy.port";
	public static String HTTP_PROXY_ENABLE = "org.eclipse.update.core.proxy.enable";

	/**
	 * The constructor.
	 */
	public UpdateCore(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static UpdateCore getPlugin() {
		return plugin;
	}
	
	/**
	 * Returns the manager that manages URL connection threads.
	 */
	public ConnectionThreadManager getConnectionManager() {
		if (connectionManager==null)
			connectionManager = new ConnectionThreadManager();
		return connectionManager;
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
		
		//
		try {
			File logFile = getUpdateStateLocation();
			if (logFile!=null)
				log = new UpdateManagerLogWriter(logFile);
		} catch (IOException e){
			warn("",e);
		}
		
		SiteManager.setHttpProxyInfo(
			getPluginPreferences().getBoolean(HTTP_PROXY_ENABLE),
			getPluginPreferences().getString(HTTP_PROXY_HOST),
			getPluginPreferences().getString(HTTP_PROXY_PORT));
	}
 
	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		
		JarContentReference.shutdown(); // make sure we are not leaving jars open
		Utilities.shutdown(); // cleanup temp area
		if (log!=null)
			log.shutdown();
		if (connectionManager!=null)
			connectionManager.shutdown();
					
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
		StringBuffer msg = new StringBuffer();
		msg.append(getPlugin().toString());
		msg.append("^");
		msg.append(Integer.toHexString(Thread.currentThread().hashCode()));
		msg.append(" ");
		msg.append(s);
		System.out.println(msg.toString());
	}
	
	/**
	 * Dumps a String in the log if WARNING is set to true
	 */
	public static void warn(String s) {
		if (DEBUG && DEBUG_SHOW_WARNINGS) {
			if (s!=null){
				s="WARNING: "+s;
			}
			log(s, null); 
		}
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
				s="UPDATE MANAGER INFO: "+s;
			}
			log(s,e);
		}
	}
			
	/**
	 * Logs a status
	 */
	public static void log(IStatus status){
		UpdateCore.getPlugin().getLog().log(status);		
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
	/*
	 * Method log.
	 * @param newConfiguration
	 */
	public static void log(IInstallConfiguration newConfiguration) {
		if (log!=null)
			log.log(newConfiguration);
	}

	/*
	 * Get update log location relative to platform configuration
	 */
	private static File getUpdateStateLocation() throws IOException {
		
		IPlatformConfiguration config = BootLoader.getCurrentPlatformConfiguration();		
		URL configurationLocation = config.getConfigurationLocation();
		if (configurationLocation==null){
			warn("Unable to retrieve location for update manager log file");
			return null;
		}
		URL configLocation = Platform.resolve(configurationLocation);
		File updateStateLocation = null;

		if ("file".equalsIgnoreCase(configLocation.getProtocol())) {
			// ensure path exists. Handle transient configurations
			ArrayList list = new ArrayList();
			File path = new File(configLocation.getFile());
			updateStateLocation = new File(path.getParentFile(),LOG_FILE);
			while (path != null) { // walk up to first dir that exists
				if (!path.exists()) {
					list.add(path);
					path = path.getParentFile();
				} else
					path = null;
			}
			for (int i = list.size() - 1; i >= 0; i--) { // walk down to create missing dirs
				path = (File) list.get(i);
				path.mkdir();
				if (config.isTransient())
					path.deleteOnExit();
			}
		}
		return updateStateLocation;
	}

	/**
	 * Sends the GET request to the server and returns the server's
	 * response.
	 *
	 * @param url the URL to open on the server
	 * @return the server's response
	 * @throws IOException if an I/O error occurs. Reasons include:
	 * <ul>
	 * <li>The client is closed.
	 * <li>The client could not connect to the server
	 * <li>An I/O error occurs while communicating with the server
	 * <ul>
	 */
	public Response get(URL url) throws IOException {
		//Request request = null;
		Response response = null;
		
		if ("file".equals(url.getProtocol())){
			response = new FileResponse(url.openStream());
		} else {
			response = new HttpResponse(url);
		}
		
		/*else {
			try {
				request = new Request("GET", url, null);
				response = client.invoke(request);
			} finally {
				if (request != null) {
					try {
						request.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}*/
		return response;
	}
	
	/*
	 * Returns true if the feature is a patch
	 */
	public static boolean isPatch(IFeature candidate) {
		IImport[] imports = candidate.getImports();

		for (int i = 0; i < imports.length; i++) {
			IImport iimport = imports[i];
			if (iimport.isPatch())
				return true;
		}
		return false;
	}
	
	/**
	 * Initializes the default preferences settings for this plug-in.
	 * <p>
	 * This method is called sometime after the preference store for this
	 * plug-in is created. Default values are never stored in preference
	 * stores; they must be filled in each time. This method provides the
	 * opportunity to initialize the default values.
	 * </p>
	 * <p>
	 * The default implementation of this method does nothing. A subclass that needs
	 * to set default values for its preferences must reimplement this method.
	 * Default values set at a later point will override any default override
	 * settings supplied from outside the plug-in (product configuration or
	 * platform start up).
	 * </p>
	 */
	protected void initializeDefaultPluginPreferences() {
		getPluginPreferences().setDefault(P_CHECK_SIGNATURE, true);		
		getPluginPreferences().setDefault(P_HISTORY_SIZE, 50);
		getPluginPreferences().setDefault(P_UPDATE_VERSIONS, EQUIVALENT_VALUE);
	}
}
