/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.core.services.ISchedulingExecutor;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
	/**
	 * The bundle symbolic name.
	 */
	public static final String PI_WORKBENCH = "org.eclipse.e4.ui.worbkench"; //$NON-NLS-1$

	private static Activator activator;

	private BundleContext context;
	private ServiceRegistration executorTracker;
	private ServiceTracker locationTracker;
	private ServiceTracker pkgAdminTracker;

	/**
	 * Get the default activator.
	 * 
	 * @return a BundleActivator
	 */
	public static Activator getDefault() {
		return activator;
	}

	/**
	 * @return the bundle object
	 */
	public Bundle getBundle() {
		return context.getBundle();
	}

	/**
	 * @return the PackageAdmin service from this bundle
	 */
	public PackageAdmin getBundleAdmin() {
		if (pkgAdminTracker == null) {
			if (context == null)
				return null;
			pkgAdminTracker = new ServiceTracker(context, PackageAdmin.class
					.getName(), null);
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

	/**
	 * @return this bundles context
	 */
	public BundleContext getContext() {
		return context;
	}

	/**
	 * @return the instance Location service
	 */
	public Location getInstanceLocation() {
		if (locationTracker == null) {
			Filter filter = null;
			try {
				filter = context.createFilter(Location.INSTANCE_FILTER);
			} catch (InvalidSyntaxException e) {
				// ignore this. It should never happen as we have tested the
				// above format.
			}
			locationTracker = new ServiceTracker(context, filter, null);
			locationTracker.open();
		}
		return (Location) locationTracker.getService();
	}

	public void start(BundleContext context) throws Exception {
		activator = this;
		this.context = context;
		executorTracker = context.registerService(
				ISchedulingExecutor.SERVICE_NAME, new JobExecutor(), null);
	}

	public void stop(BundleContext context) throws Exception {
		if (pkgAdminTracker != null) {
			pkgAdminTracker.close();
			pkgAdminTracker = null;
		}
		if (locationTracker != null) {
			locationTracker.close();
			locationTracker = null;
		}
		if (executorTracker != null) {
			executorTracker.unregister();
			executorTracker = null;
		}
	}

}
