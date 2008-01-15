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

import java.net.URL;
import java.util.*;
import org.eclipse.core.internal.runtime.auth.AuthorizationHandler;
import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
	/**
	 * The identifier of the descriptor of this plugin in plugin.xml.
	 */
	public static final String ID = "org.eclipse.core.net"; //$NON-NLS-1$
	
	public static final String PT_AUTHENTICATOR = "authenticator"; //$NON-NLS-1$
	
	private static final String PROP_REGISTER_SERVICE = "org.eclipse.net.core.enableProxyService"; //$NON-NLS-1$

	private static BundleContext bundleContext;
	private static ServiceTracker extensionRegistryTracker;
	private static ServiceTracker logTracker;
	private ServiceRegistration proxyService;

	/*
	 * Return this bundle's context, or null.
	 */
	public static BundleContext getBundleContext() {
		return bundleContext;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		if (Boolean.valueOf(System.getProperty(PROP_REGISTER_SERVICE, "true")).booleanValue()) { //$NON-NLS-1$
			ProxyManager proxyManager = (ProxyManager)ProxyManager.getProxyManager();
			proxyManager.initialize();
			proxyService = getBundleContext().registerService(IProxyService.class.getName(), proxyManager, new Hashtable());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		if (proxyService != null) {
			proxyService.unregister();
			proxyService = null;
		}
		if (extensionRegistryTracker != null) {
			extensionRegistryTracker.close();
			extensionRegistryTracker = null;
		}
		if (logTracker != null) {
			logTracker.close();
			logTracker = null;
		}
		bundleContext = null;
	}

	/*
	 * Log the given message as an error.
	 */
	public static void logError(String message, Throwable exc) {
		log(new Status(IStatus.ERROR, ID, 0, message, exc));
	}

	/*
	 * Log the given message as an informational message.
	 */
	public static void logInfo(String message, Throwable exc) {
		log(new Status(IStatus.INFO, ID, 0, message, exc));
	}

	public static org.osgi.service.prefs.Preferences getInstancePreferences() {
		return new InstanceScope().getNode(ID);
	}

	public static void log(int severity, String message, Throwable throwable) {
		log(new Status(severity, ID, 0, message, throwable));
	}

	/*
	 * Return the extension registry. It is acquired lazily.
	 */
	public static IExtensionRegistry getExtensionRegistry() {
		if (extensionRegistryTracker == null) {
			extensionRegistryTracker = new ServiceTracker(getBundleContext(), IExtensionRegistry.class.getName(), null);
			extensionRegistryTracker.open();
		}
		return (IExtensionRegistry) extensionRegistryTracker.getService();
	}

	/*
	 * TODO: This currently references internal classes but will be replaced by the new security work
	 * to be available in Eclipse 3.4.
	 */
	public static void addAuthorizationInfo(URL serverUrl, String realm, String authScheme, Map info) throws CoreException {
		AuthorizationHandler.addAuthorizationInfo(serverUrl, realm, authScheme, info);
	}

	/*
	 * TODO: This currently references internal classes but will be replaced by the new security work
	 * to be available in Eclipse 3.4.
	 */
	public static Map getAuthorizationInfo(URL serverUrl, String realm, String authScheme) {
		return AuthorizationHandler.getAuthorizationInfo(serverUrl, realm, authScheme);
	}
	
	/*
	 * TODO: This currently references internal classes but will be replaced by the new security work
	 * to be available in Eclipse 3.4.
	 */
	public static void flushAuthorizationInfo(URL serverUrl, String realm, String authScheme) throws CoreException {
		AuthorizationHandler.flushAuthorizationInfo(serverUrl, realm, authScheme);
	}

	/*
	 * Log the given status to the log file. If the log is not available, log the status to the console.
	 */
	public static void log(IStatus status) {
		if (logTracker == null) {
			logTracker = new ServiceTracker(getBundleContext(), FrameworkLog.class.getName(), null);
			logTracker.open();
		}
		FrameworkLog log = (FrameworkLog) logTracker.getService();
		if (log != null) {
			log.log(getLog(status));
		} else {
			System.out.println(status.getMessage());
			if (status.getException() != null)
				status.getException().printStackTrace();
		}
	}

	/**
	 * Copied from PlatformLogWriter in core runtime.
	 */
	private static FrameworkLogEntry getLog(IStatus status) {
		Throwable t = status.getException();
		ArrayList childlist = new ArrayList();

		int stackCode = t instanceof CoreException ? 1 : 0;
		// ensure a substatus inside a CoreException is properly logged 
		if (stackCode == 1) {
			IStatus coreStatus = ((CoreException) t).getStatus();
			if (coreStatus != null) {
				childlist.add(getLog(coreStatus));
			}
		}

		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				childlist.add(getLog(children[i]));
			}
		}

		FrameworkLogEntry[] children = (FrameworkLogEntry[]) (childlist.size() == 0 ? null : childlist.toArray(new FrameworkLogEntry[childlist.size()]));

		return new FrameworkLogEntry(status.getPlugin(), status.getSeverity(), status.getCode(), status.getMessage(), stackCode, t, children);
	}

}
