/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.tests;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.appserver.WebappManager;
import org.eclipse.update.internal.core.UpdateCore;
import org.osgi.framework.*;

/**
 * manages the startuo and shutown of the 
 * web server
 */
public class UpdateTestsPlugin extends Plugin {

	private static String appServerHost = null;
	private static int appServerPort = 0;
	private static UpdateTestsPlugin plugin;
	private static BundleContext bundleContext;
	private static boolean initialized=false;

	public static UpdateTestsPlugin getPlugin() {
		return plugin;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		WebappManager.stop("org.eclipse.update.tests.core.updatetests");
		plugin = null;
		bundleContext = null;
		super.stop(context);
	}

	/**
	 * Returns the host identifier for the web app server
	 */
	public static String getWebAppServerHost() {
		if (!initialized) initialize();
		return appServerHost;
	}

	/**
	 * Returns the port identifier for the web app server
	 */
	public static int getWebAppServerPort() {
		if (!initialized) initialize();		
		return appServerPort;
	}
	/**
	 * Method initialize.
	 */
	private static void initialize() {
		String text = null;
		try {
			WebappManager.start("org.eclipse.update.tests.core.updatetests", "org.eclipse.update.tests.core", new Path("webserver"));
			appServerHost = WebappManager.getHost();
			appServerPort = WebappManager.getPort();

			text = "The webServer did start ip:" + appServerHost + ":" + appServerPort;
		} catch (CoreException e) {
			text = "The webServer didn't start ";
			IStatus status = new Status(IStatus.ERROR, "org.eclipse.update.tests.core", IStatus.OK, "WebServer not started. Update Tests results are invalid", null);
			UpdateCore.warn("",new CoreException(status));
		}finally {
			System.out.println(text);
			initialized = true;
		}
	}

}
