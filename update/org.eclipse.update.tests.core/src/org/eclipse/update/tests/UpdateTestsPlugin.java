package org.eclipse.update.tests;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.appserver.WebappManager;

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

	public static UpdateTestsPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Called by Platform after loading the plugin
	 */
	public void startup() throws CoreException {

		try {
			WebappManager.start("org.eclipse.update.tests.core.updatetests", "org.eclipse.update.tests.core", new Path("webserver"));
			appServerHost = WebappManager.getHost();
			appServerPort = WebappManager.getPort();

			String text = "The webServer did start ip:" + getWebAppServerHost() + ":" + getWebAppServerPort();
		} catch (CoreException e) {
			String text = "The webServer didn't start ip:" + getWebAppServerHost() + ":" + getWebAppServerPort();
			IStatus status = new Status(IStatus.ERROR, "org.eclipse.update.tests.core", IStatus.OK, "WebServer not started. Update Tests results are invalid", null);
			throw new CoreException(status);
		}
	}

	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public void shutdown() throws CoreException {
		WebappManager.stop("org.eclipse.update.tests.core.updatetests");
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