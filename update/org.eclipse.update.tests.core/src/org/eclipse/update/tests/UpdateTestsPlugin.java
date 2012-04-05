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
package org.eclipse.update.tests;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.update.internal.core.UpdateCore;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * manages the startup and shutown of the 
 * web server
 */
public class UpdateTestsPlugin extends Plugin {

	private static String appServerHost = null;
	private static int appServerPort = 0;
	private static UpdateTestsPlugin plugin;
	private static boolean initialized=false;
	private static ServiceTracker httpServiceTracker;

	public static UpdateTestsPlugin getPlugin() {
		return plugin;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		httpServiceTracker = new ServiceTracker(context, HttpService.class.getName(), null);
		httpServiceTracker.open();
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		
		if(httpServiceTracker != null)
			httpServiceTracker.close();
		
		httpServiceTracker = null;
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

			// ensure that the http stuff is started
			Dictionary d = new Hashtable();
			d.put("http.port", new Integer(0)); //$NON-NLS-1$
			JettyConfigurator.startServer("updateTests", d);
			ensureBundleStarted("org.eclipse.equinox.http.registry"); //$NON-NLS-1$
			
			ServiceReference reference = 
				httpServiceTracker.getServiceReference();
			
			String port = (String) reference.getProperty("http.port"); //$NON-NLS-1$
			
			appServerHost = "localhost"; //$NON-NLS-1$
			appServerPort = Integer.parseInt(port);

			text = "The webServer did start ip:" + appServerHost + ":" + appServerPort;
		} catch (Exception e) {
			text = "The webServer didn't start ";
			IStatus status = new Status(IStatus.ERROR, "org.eclipse.update.tests.core", IStatus.OK, "WebServer not started. Update Tests results are invalid", null);
			UpdateCore.warn("",new CoreException(status));
		} finally {
			System.out.println(text);
			initialized = true;
		}
	}
	
	private static void ensureBundleStarted(String symbolicName) throws BundleException {
		Bundle bundle = Platform.getBundle(symbolicName);
		if (bundle != null) {
			if (bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.STARTING) {
				bundle.start();
			}
		}
	}

}
