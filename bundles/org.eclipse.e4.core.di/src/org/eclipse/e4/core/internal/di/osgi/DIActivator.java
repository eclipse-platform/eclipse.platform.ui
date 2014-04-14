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
package org.eclipse.e4.core.internal.di.osgi;

import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class DIActivator implements BundleActivator {

	static private DIActivator defaultInstance;
	private BundleContext bundleContext;
	private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;
	private ServiceTracker<FrameworkLog, FrameworkLog> logTracker = null;

	public DIActivator() {
		defaultInstance = this;
	}

	public static DIActivator getDefault() {
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
		if (logTracker != null) {
			logTracker.close();
			logTracker = null;
		}

		bundleContext = null;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}

	public boolean getBooleanDebugOption(String option, boolean defaultValue) {
		if (debugTracker == null) {
			debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(bundleContext, DebugOptions.class, null);
			debugTracker.open();
		}
		DebugOptions options = debugTracker.getService();
		if (options != null) {
			String value = options.getOption(option);
			if (value != null)
				return value.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
		return defaultValue;
	}

	public FrameworkLog getFrameworkLog() {
		if (logTracker == null) {
			if (bundleContext == null)
				return null;
			logTracker = new ServiceTracker<FrameworkLog, FrameworkLog>(bundleContext, FrameworkLog.class, null);
			logTracker.open();
		}
		return logTracker.getService();
	}

}
