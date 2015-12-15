/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	private ServiceTracker packageAdminTracker;
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.e4.ui.tests";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (packageAdminTracker != null) {
			packageAdminTracker.close();
			packageAdminTracker = null;
		}
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}



	public PackageAdmin getPackageAdmin() {
		if (packageAdminTracker == null) {
			BundleContext bundleContext = plugin.getBundle().getBundleContext();
			if (bundleContext == null)
				return null;
			packageAdminTracker = new ServiceTracker(bundleContext,
					PackageAdmin.class.getName(), null);
			packageAdminTracker.open();
		}
		return (PackageAdmin) packageAdminTracker.getService();
	}

	/**
	 * Generate a platform URI referencing the provided class.
	 *
	 * @param clazz
	 *            the class to be referenced
	 * @return the platform-based URI: bundleclass://X/X.Y
	 */
	public static String asURI(Class<?> clazz) {
		PackageAdmin pkgadm = getDefault().getPackageAdmin();
		return "bundleclass://" + pkgadm.getBundle(clazz).getSymbolicName()
				+ '/' + clazz.getName();
	}

}
