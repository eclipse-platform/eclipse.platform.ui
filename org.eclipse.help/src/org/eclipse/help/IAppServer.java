/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help;


public interface IAppServer
{
	/**
	 * Adds the specified webapp from a plugin.
	 * <p> It is assumed that webapp names are unique. It is suggested to
	 * create unique web app names by prefixing them with the plugin id.</p>
	 * @param webAppName the name of the web application (context name)
	 * @param plugin the plugin identifier of the webapp
	 * @param path the plugin relative path where the web app WAR or directory is located
	 * @return true if the webapp was added, false otherwise
	 */
	boolean add(String webAppName, String plugin, String path);
	
	/**
	 * Removes the specified webapp
	 * @param webApp the name of the web app
	 * @param plugin the plugin containg the web app. Not used now.
	 */
	void remove(String webApp, String plugin);

	/**
	 * Returns the host name or ip the app server runs on.
	 */
	String getHost();
	
	/**
	 * Returns the port number the app server listens on.
	 */
	int getPort();
	
	/**
	 * Sets the host and port for the app server. Must be called before adding a webapp,
	 * but the call is optional.
	 * @param host the host name or IP address. Pass null when any address on 
	 *         local machine is to be used.
	 * @param port the port number to be used. Pass 0 to let the system select
	 *         an available port.
	 */
	void setAddress(String host, int port);
}