/*******************************************************************************
 *  Copyright (c) 2009, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.services;

import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

//there is no replacement for PackageAdmin#getBundles()
@SuppressWarnings("deprecation")
public class ServicesActivator implements BundleActivator {

	static private ServicesActivator defaultInstance;
	private BundleContext bundleContext;
	private ServiceTracker<PackageAdmin, PackageAdmin> pkgAdminTracker;
	private ServiceTracker<LogService, LogService> logTracker;
	private ServiceTracker<BundleLocalization, BundleLocalization> localizationTracker = null;

	public ServicesActivator() {
		defaultInstance = this;
	}

	public static ServicesActivator getDefault() {
		return defaultInstance;
	}

	public void start(BundleContext context) throws Exception {
		bundleContext = context;
	}

	public void stop(BundleContext context) throws Exception {
		if (pkgAdminTracker != null) {
			pkgAdminTracker.close();
			pkgAdminTracker = null;
		}
		if (localizationTracker != null) {
			localizationTracker.close();
			localizationTracker = null;
		}
		if (logTracker != null) {
			logTracker.close();
			logTracker = null;
		}
		bundleContext = null;
	}

	public PackageAdmin getPackageAdmin() {
		if (pkgAdminTracker == null) {
			if (bundleContext == null)
				return null;
			pkgAdminTracker = new ServiceTracker<PackageAdmin, PackageAdmin>(bundleContext,
					PackageAdmin.class, null);
			pkgAdminTracker.open();
		}
		return (PackageAdmin) pkgAdminTracker.getService();
	}

	public LogService getLogService() {
		if (logTracker == null) {
			if (bundleContext == null)
				return null;
			logTracker = new ServiceTracker<LogService, LogService>(bundleContext,
					LogService.class, null);
			logTracker.open();
		}
		return logTracker.getService();
	}

	public BundleLocalization getLocalizationService() {
		if (localizationTracker == null) {
			if (bundleContext == null)
				return null;
			localizationTracker = new ServiceTracker<BundleLocalization, BundleLocalization>(
					bundleContext, BundleLocalization.class, null);
			localizationTracker.open();
		}
		return localizationTracker.getService();
	}
}
