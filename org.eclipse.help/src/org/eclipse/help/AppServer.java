/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help;

/**
 * Singleton class for obtaining the platform app server.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @deprecated API moved to org.eclipse.tomcat plug-in
 * @see org.eclipse.tomcat.AppServer
 */
public class AppServer {
	/**
	 * Adds the specified webapp from a plugin.
	 * <p> It is assumed that webapp names are unique. It is suggested to
	 * create unique web app names by prefixing them with the plugin id.</p>
	 * @param webAppName the name of the web application (context name)
	 * @param plugin the plugin identifier of the webapp
	 * @param path the plugin relative path where the web app WAR or directory is located
	 * @return true if the webapp was added, false otherwise
	 */
	public static synchronized boolean add(
		String webAppName,
		String plugin,
		String path) {
		return org.eclipse.tomcat.AppServer.add(webAppName, plugin, path);
	}

	/**
	 * Removes the specified webapp.
	 * @param webApp the name of the web app
	 * @param plugin the plugin containg the web app. Not used now.
	 */
	public static void remove(String webApp, String plugin) {
		org.eclipse.tomcat.AppServer.remove(webApp, plugin);
	}

	/**
	 * Returns the host name or ip the app server runs on.
	 * @return String representaion of host name of IP,
	 *  null if server not started yet
	 */
	public static String getHost() {
		return org.eclipse.tomcat.AppServer.getHost();
	}

	/**
	 * Returns the port number the app server listens on.
	 * @return integer port number,
	 *  0 if server not started
	 */
	public static int getPort() {
		return org.eclipse.tomcat.AppServer.getPort();
	}

	/**
	 * Checks if the app server is running.
	 * @return true if the app server is running
	 */
	public static boolean isRunning() {
		return org.eclipse.tomcat.internal.TomcatPlugin.getDefault().getAppServer().isRunning();

	}

}