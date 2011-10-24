/*******************************************************************************
 *  Copyright (c) 2009, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.osgi;

import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class ContextsActivator implements BundleActivator {

	static private ContextsActivator defaultInstance;
	private BundleContext bundleContext;
	private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;

	public ContextsActivator() {
		defaultInstance = this;
	}

	public static ContextsActivator getDefault() {
		return defaultInstance;
	}

	public void start(BundleContext context) throws Exception {
		bundleContext = context;
	}

	public void stop(BundleContext context) throws Exception {
		if (debugTracker != null) {
			debugTracker.close();
			debugTracker = null;
		}
		bundleContext = null;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public static boolean getBooleanDebugOption(String option, boolean defaultValue) {
		BundleContext myBundleContext = getDefault().bundleContext;
		if (myBundleContext == null)
			return defaultValue;
		if (getDefault().debugTracker == null) {
			getDefault().debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(getDefault().bundleContext, DebugOptions.class.getName(), null);
			getDefault().debugTracker.open();
		}
		DebugOptions options = getDefault().debugTracker.getService();
		if (options != null) {
			String value = options.getOption(option);
			if (value != null)
				return value.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
		return defaultValue;
	}

}
