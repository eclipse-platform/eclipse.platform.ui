/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp;

import org.eclipse.core.runtime.*;
import org.osgi.framework.*;

/**
 * Welp web application plug-in.
 */
public class HelpWebappPlugin extends Plugin {
	public final static String PLUGIN_ID = "org.eclipse.help.webapp"; //$NON-NLS-1$

	// debug options
	public static boolean DEBUG = false;

	public static boolean DEBUG_WORKINGSETS = false;

	protected static HelpWebappPlugin plugin;

	//private static BundleContext bundleContext;

	/**
	 * Logs an Error message with an exception.
	 */
	public static synchronized void logError(String message, Throwable ex) {
		if (message == null)
			message = ""; //$NON-NLS-1$
		Status errorStatus = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK,
				message, ex);
		HelpWebappPlugin.getDefault().getLog().log(errorStatus);
	}

	/**
	 * Logs a Warning message with an exception. Note that the message should
	 * already be localized to proper locale. ie: WebappResources.getString()
	 * should already have been called
	 */
	public static synchronized void logWarning(String message) {
		if (HelpWebappPlugin.DEBUG) {
			if (message == null)
				message = ""; //$NON-NLS-1$
			Status warningStatus = new Status(IStatus.WARNING, PLUGIN_ID,
					IStatus.OK, message, null);
			HelpWebappPlugin.getDefault().getLog().log(warningStatus);
		}
	}

	/**
	 * @return the singleton instance of the help webapp plugin
	 */
	public static HelpWebappPlugin getDefault() {
		return plugin;
	}

	private static BundleContext bundleContext;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		bundleContext = context;
		// Setup debugging options
		// Setup debugging options
		DEBUG = isDebugging();
		if (DEBUG) {
			DEBUG_WORKINGSETS = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.help.webapp/debug/workingsets")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		//bundleContext = null;
		super.stop(context);
	}
	
	public static BundleContext getContext() {
		return bundleContext;
	}
}
