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

package org.eclipse.core.internal.net;

import java.util.Hashtable;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator extends Plugin {
	/**
	 * The identifier of the descriptor of this plugin in plugin.xml.
	 */
	public static final String ID = "org.eclipse.core.net"; //$NON-NLS-1$
	
	public static final String PT_AUTHENTICATOR = "authenticator"; //$NON-NLS-1$
	
	private static final String PROP_REGISTER_SERVICE = "org.eclipse.net.core.enableProxyService"; //$NON-NLS-1$

	/**
	 * The instance of this plugin.
	 */
	private static Activator instance;

	private ServiceRegistration proxyService;

	/**
	 * Constructor for use by the Eclipse platform only.
	 */
	public Activator() {
		super();
		instance = this;
	}

	/**
	 * Returns the instance of this plugin.
	 * @return the singleton instance of this plug-in class
	 */
	static public Activator getInstance() {
		return instance;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		if (Boolean.valueOf(System.getProperty(PROP_REGISTER_SERVICE, "true")).booleanValue()) { //$NON-NLS-1$
			ProxyManager proxyManager = (ProxyManager)ProxyManager.getProxyManager();
			proxyManager.initialize();
			proxyService = getBundle().getBundleContext().registerService(IProxyService.class.getName(), proxyManager, new Hashtable());
		}
	}
	
	public void stop(BundleContext context) throws Exception {
		if (proxyService != null) {
			proxyService.unregister();
			proxyService = null;
		}
		super.stop(context);
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

	public static void log(int severity, String message, Throwable throwable) {
		getInstance().getLog().log(new Status(severity, ID, 0, message, throwable));
	}
}
