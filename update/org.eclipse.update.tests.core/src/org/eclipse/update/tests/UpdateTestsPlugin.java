package org.eclipse.update.tests;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.AppServer;
import org.eclipse.help.IAppServer;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

/**
 * manages the startuo and shutown of the 
 * web server
 */
public class UpdateTestsPlugin extends Plugin {

	private static String appServerHost = null;
	private static int appServerPort = 0;
	private static UpdateTestsPlugin plugin;

	public UpdateTestsPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	public static UpdateTestsPlugin getPlugin(){
		return plugin;
	}

	/**
	 * Called by Platform after loading the plugin
	 */
	public void startup() throws CoreException {
		AppServer.add("updatetests", "org.eclipse.update.tests.core", "webserver");		
		appServerHost = AppServer.getHost();
		appServerPort = AppServer.getPort();
	}

	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public void shutdown() throws CoreException {
		AppServer.remove("updatetests", "org.eclipse.update.tests.core");				
		super.shutdown();		
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

}