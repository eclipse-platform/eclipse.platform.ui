/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The class contains a set of helper methods for the runtime Jobs plugin.
 * The following utility methods are supplied:
 * - provides access to debug options
 * - provides some bundle discovery functionality
 * 
 * The closeServices() method should be called before the plugin is stopped. 
 * 
 * @since org.eclipse.core.jobs 3.2
 */
public class JobOSGiUtils {
	private ServiceTracker debugTracker = null;
	private ServiceTracker bundleTracker = null;

	private static final JobOSGiUtils singleton = new JobOSGiUtils();

	public static JobOSGiUtils getDefault() {
		return singleton;
	}

	/**
	 * Private constructor to block instance creation.
	 */
	private JobOSGiUtils() {
		super();

		try {
			initServices();
		} catch (ClassNotFoundException e) {
			// expected if OSGi is not present
		}
	}

	private void initServices() throws ClassNotFoundException {
		BundleContext context = Activator.getContext();
		if (context == null) {
			JobMessages.message("JobsOSGiUtils called before plugin started"); //$NON-NLS-1$
			return;
		}

		debugTracker = new ServiceTracker(context, DebugOptions.class.getName(), null);
		debugTracker.open();

		bundleTracker = new ServiceTracker(context, PackageAdmin.class.getName(), null);
		bundleTracker.open();
	}

	void closeServices() throws ClassNotFoundException {
		if (debugTracker != null) {
			debugTracker.close();
			debugTracker = null;
		}
		if (bundleTracker != null) {
			bundleTracker.close();
			bundleTracker = null;
		}
	}

	public boolean getBooleanDebugOption(String option, boolean defaultValue) throws ClassNotFoundException {
		if (debugTracker == null) {
			JobMessages.message("Debug tracker is not set"); //$NON-NLS-1$
			return defaultValue;
		}
		DebugOptions options = (DebugOptions) debugTracker.getService();
		if (options != null) {
			String value = options.getOption(option);
			if (value != null)
				return value.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
		return defaultValue;
	}

	/**
	 * Returns the bundle id of the bundle that contains the provided object, or
	 * <code>null</code> if the bundle could not be determined.
	 */
	public String getBundleId(Object object) throws ClassNotFoundException {
		if (bundleTracker == null) {
			JobMessages.message("Bundle tracker is not set"); //$NON-NLS-1$
			return null;
		}
		PackageAdmin packageAdmin = (PackageAdmin) bundleTracker.getService();
		if (object == null)
			return null;
		if (packageAdmin == null)
			return null;
		Bundle source = packageAdmin.getBundle(object.getClass());
		if (source != null && source.getSymbolicName() != null)
			return source.getSymbolicName();
		return null;
	}

}
