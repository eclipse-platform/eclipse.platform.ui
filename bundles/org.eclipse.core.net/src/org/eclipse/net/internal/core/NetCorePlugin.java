/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * yyyymmdd bug      Email and other contact information
 * -------- -------- -----------------------------------------------------------
 * 20070119   161112 makandre@ca.ibm.com - Andrew Mak, WSE: can't find business thru a proxy server that needs basic auth
 * 20070201   154100 pmoogk@ca.ibm.com - Peter Moogk, Port internet code from WTP to Eclipse base.
 *******************************************************************************/

package org.eclipse.net.internal.core;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.net.core.NetCore;
import org.osgi.framework.BundleContext;

public class NetCorePlugin extends Plugin {
	/**
	 * The identifier of the descriptor of this plugin in plugin.xml.
	 */
	public static final String ID = "org.eclipse.net.core"; //$NON-NLS-1$

	/**
	 * The instance of this plugin.
	 */
	private static NetCorePlugin instance;

	/**
	 * Constructor for use by the Eclipse platform only.
	 */
	public NetCorePlugin() {
		super();
		instance = this;
	}

	/**
	 * Returns the instance of this plugin.
	 * @return the singleton instance of this plug-in class
	 */
	static public NetCorePlugin getInstance() {
		return instance;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		((ProxyManager)NetCore.getProxyManager()).initialize();
	}
	
	public static void logError(String message, Throwable exc) {
		IStatus status = new Status(IStatus.ERROR, ID, 0, message, exc);

		getInstance().getLog().log(status);
	}

	public static void logInfo(String message, Throwable exc) {
		IStatus status = new Status(IStatus.INFO, ID, 0, message, exc);

		getInstance().getLog().log(status);
	}

	public org.osgi.service.prefs.Preferences getInstancePreferences() {
		return new InstanceScope().getNode(getBundle().getSymbolicName());
	}
}
