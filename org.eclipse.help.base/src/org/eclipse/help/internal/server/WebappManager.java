/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.server;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class WebappManager {

	private static String host;
	private static int port = -1;
	
	public static void start(String webappName) throws CoreException {
		Dictionary d = new Hashtable();
		
		// configure the port
		d.put("http.port", new Integer(getPort())); //$NON-NLS-1$

		// set the base URL
		d.put("context.path", "/help"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// prevent Jetty from also starting on port 80
		System.setProperty("org.eclipse.equinox.http.jetty.autostart", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// suppress Jetty INFO/DEBUG messages to stderr
		Logger.getLogger("org.mortbay").setLevel(Level.WARNING); //$NON-NLS-1$
		
		try {
			JettyConfigurator.startServer(webappName, d);
			ensureBundleStarted("org.eclipse.equinox.http.registry"); //$NON-NLS-1$
		}
		catch (Exception e) {
			HelpBasePlugin.logError("An error occured while starting the help server", e); //$NON-NLS-1$
		}
	}

	public static void stop(String webappName) throws CoreException {
		try {
			JettyConfigurator.stopServer(webappName);
		}
		catch (Exception e) {
			HelpBasePlugin.logError("An error occured while stopping the help server", e); //$NON-NLS-1$
		}
	}

	public static int getPort() {
		if (port == -1) {
			String portCommandLineOverride = System.getProperty("server_port"); //$NON-NLS-1$
			if (portCommandLineOverride != null && portCommandLineOverride.trim().length() > 0) {
				try {
					port = Integer.parseInt(portCommandLineOverride);
				}
				catch (NumberFormatException e) {
					String msg = "Help server port specified in VM arguments is invalid (" + portCommandLineOverride + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					HelpBasePlugin.logError(msg, e);
				}
			}
			if (port == -1) {
				port = SocketUtil.findUnusedLocalPort();
			}
		}
		return port;
	}

	public static String getHost() {
		if (host == null) {
			String hostCommandLineOverride = System.getProperty("server_host"); //$NON-NLS-1$
			if (hostCommandLineOverride != null && hostCommandLineOverride.trim().length() > 0) {
				host = hostCommandLineOverride;
			}
			else {
				host = "127.0.0.1"; //$NON-NLS-1$
			}
		}
		return host;
	}
	
	private WebappManager() {
	}
	
	/*
	 * Ensures that the bundle with the specified name and the highest available
	 * version is started.
	 */
	private static void ensureBundleStarted(String symbolicName) throws BundleException {
		Bundle bundle = Platform.getBundle(symbolicName);
		if (bundle != null) {
			if (bundle.getState() == Bundle.RESOLVED) {
				bundle.start();
			}
		}
	}
}
