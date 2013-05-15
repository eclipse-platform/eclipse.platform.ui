/*******************************************************************************
 *  Copyright (c) 2010, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class CSSActivator implements BundleActivator {

	private static CSSActivator activator;

	private BundleContext context;
	private ServiceTracker pkgAdminTracker;
	private ServiceTracker logTracker;

	public static CSSActivator getDefault() {
		return activator;
	}

	public Bundle getBundle() {
		return context.getBundle();
	}

	public PackageAdmin getBundleAdmin() {
		if (pkgAdminTracker == null) {
			if (context == null)
				return null;
			pkgAdminTracker = new ServiceTracker(context, PackageAdmin.class.getName(), null);
			pkgAdminTracker.open();
		}
		return (PackageAdmin) pkgAdminTracker.getService();
	}

	/**
	 * @param bundleName
	 *            the bundle id
	 * @return A bundle if found, or <code>null</code>
	 */
	public Bundle getBundleForName(String bundleName) {
		Bundle[] bundles = getBundleAdmin().getBundles(bundleName, null);
		if (bundles == null)
			return null;
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	public BundleContext getContext() {
		return context;
	}

	public void start(BundleContext context) throws Exception {
		activator = this;
		this.context = context;
	}

	public void stop(BundleContext context) throws Exception {
		if (pkgAdminTracker != null) {
			pkgAdminTracker.close();
			pkgAdminTracker = null;
		}
		if (logTracker != null) {
			logTracker.close();
			logTracker = null;
		}
		context = null;
	}

	private LogService getLogger() {
		if (logTracker == null) {
			if (context == null)
				return null;
			logTracker = new ServiceTracker(context,
					LogService.class.getName(), null);
			logTracker.open();
		}
		return (LogService) logTracker.getService();
	}

	public void log(int logError, String message) {
		LogService logger = getLogger();
		if (logger != null) {
			logger.log(logError, message);
		}
	}	
	

}
