/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.appserver;

import org.eclipse.core.runtime.*;

/**
 * Interface to be implemented by the app servers that are contributed to the
 * org.eclipse.webapp.server extension point. The implementors of this class
 * should ensure that webapps are running in an environment in which they can
 * see their classes, the J2SE/J2EE classes, as well as classes loaded by the
 * custom class loader.
 * 
 * @since 2.1
 * @deprecated This internal interface is no longer used by the Eclipse Help 
 * system and should not be used by anyone else. It is likely to be removed 
 * in a future release. 
 * Use the HTTP service implementation provided by Equinox that is based 
 * on Jetty, see http://www.eclipse.org/equinox/server.
 */
public interface IWebappServer {
	/**
	 * Starts the server on specified host/port. Must be called before running a
	 * webapp.
	 * 
	 * @param port
	 *            port to listen to. Pass 0 to let the system pick up a port.
	 * @param host
	 *            server host. Can be an IP address or a server name
	 */
	public void start(int port, String host) throws CoreException;

	/**
	 * Stops the app server.
	 */
	public void stop() throws CoreException;

	/**
	 * Checks if the app server is running
	 */
	public boolean isRunning();

	/**
	 * Runs a webapp on the server.
	 * 
	 * @param webappName
	 *            the name of the web app (also knowns as application context)
	 * @param path
	 *            path to the webapp directory or WAR file.
	 * @param customLoader
	 *            optional class loader to add to the default webapp class
	 *            loader
	 */
	public void start(String webappName, IPath path, ClassLoader customLoader)
			throws CoreException;

	/**
	 * Stops the specified webapp.
	 */
	public void stop(String webappName) throws CoreException;

	/**
	 * Returns the port number the app server listens on.
	 * 
	 * @return integer port number, 0 if server not started
	 */
	public int getPort();

	/**
	 * Returns the host name or ip the app server runs on.
	 * 
	 * @return String representaion of host name of IP, null if server not
	 *         started yet
	 */
	public String getHost();
}
