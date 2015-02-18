/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import org.eclipse.core.internal.registry.osgi.Activator;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Helper class for interacting with {@link PackageAdmin}.
 */
public class BundleHelper {

	private ServiceTracker bundleTracker = null;

	private static final BundleHelper singleton = new BundleHelper();

	public static BundleHelper getDefault() {
		return singleton;
	}

	/**
	 * Private constructor to block instance creation.
	 */
	private BundleHelper() {
		super();
	}

	private PackageAdmin getPackageAdmin() {
		if (bundleTracker == null) {
			bundleTracker = new ServiceTracker(Activator.getContext(), PackageAdmin.class.getName(), null);
			bundleTracker.open();
		}
		return (PackageAdmin) bundleTracker.getService();
	}

	public Bundle getBundle(String symbolicName) {
		PackageAdmin packageAdmin = getPackageAdmin();
		if (packageAdmin == null)
			return null;
		Bundle[] bundles = packageAdmin.getBundles(symbolicName, null);
		if (bundles == null)
			return null;
		//Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	public Bundle[] getHosts(Bundle bundle) {
		PackageAdmin packageAdmin = getPackageAdmin();
		if (packageAdmin == null)
			return null;
		return packageAdmin.getHosts(bundle);
	}

}
