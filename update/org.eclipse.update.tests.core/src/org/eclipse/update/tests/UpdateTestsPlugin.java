package org.eclipse.update.tests;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.IAppServer;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

/**
 * manages the startuo and shutown of the 
 * web server
 */
public class UpdateTestsPlugin extends Plugin {

	private final static String APP_SERVER_EXTENSION_ID = "org.eclipse.help.app-server";
	private static final String APP_SERVER_CLASS_ATTRIBUTE = "class";
	private IAppServer appServer;
	private static UpdateTestsPlugin plugin;

	public UpdateTestsPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		// set dynamic address
		appServer = getAppServer();
		if (appServer != null){
			appServer.setAddress("127.0.0.1",1333);
			//appServer.setAddress(null,0);
		}
	}

	public static UpdateTestsPlugin getPlugin(){
		return plugin;
	}

	/**
	 * Called by Platform after loading the plugin
	 */
	public void startup() {
		// get an app server and start the help web app
		if (getAppServer() != null) {
			System.out.println("Starting server...");
			getAppServer().start();
			getAppServer().add("updatetests", "org.eclipse.update.tests.core", "webserver");
			System.out.println("HTTP server listening on host: "+appServer.getHost()+":"+appServer.getPort());			
		}
	}

	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public void shutdown() throws CoreException {
		// stop the web app
		if (getAppServer() != null) {
			getAppServer().remove("updatetests", "org.eclipse.update.tests.core");
			getAppServer().stop();
		}
		super.shutdown();
	}

	public IAppServer getAppServer() {
		if (appServer == null) {
			// Initializes the app server by getting an instance via 
			// app-server the extension point
			// get the app server extension from the system plugin registry	
			IPluginRegistry pluginRegistry = Platform.getPluginRegistry();
			IExtensionPoint point = pluginRegistry.getExtensionPoint(APP_SERVER_EXTENSION_ID);
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
			try {
				appServer = (IAppServer) elements[0].createExecutableExtension(APP_SERVER_CLASS_ATTRIBUTE);
			} catch (CoreException e) {
				UpdateManagerPlugin.getPlugin().getLog().log(e.getStatus());
			}
		}
		
	return appServer;
}

}