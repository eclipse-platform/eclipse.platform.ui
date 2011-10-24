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
package org.eclipse.e4.core.internal.tests;

import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class CoreTestsActivator implements BundleActivator {

	static private CoreTestsActivator defaultInstance;
	private BundleContext bundleContext;
	private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;
	private ServiceTracker<IPreferencesService, IPreferencesService> preferencesTracker = null;
	private ServiceTracker<EventAdmin, EventAdmin> eventAdminTracker;

	public CoreTestsActivator() {
		defaultInstance = this;
	}

	public static CoreTestsActivator getDefault() {
		return defaultInstance;
	}

	public void start(BundleContext context) throws Exception {
		bundleContext = context;
	}

	public void stop(BundleContext context) throws Exception {
		if (preferencesTracker != null) {
			preferencesTracker.close();
			preferencesTracker = null;
		}
		if (debugTracker != null) {
			debugTracker.close();
			debugTracker = null;
		}
		if (eventAdminTracker != null) {
			eventAdminTracker.close();
			eventAdminTracker = null;
		}
		bundleContext = null;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}
	public IPreferencesService getPreferencesService() {
		if (preferencesTracker == null) {
			if (bundleContext == null)
				return null;
			preferencesTracker = new ServiceTracker<IPreferencesService, IPreferencesService>(bundleContext, IPreferencesService.class.getName(), null);
			preferencesTracker.open();
		}
		return preferencesTracker.getService();
	}

	public boolean getBooleanDebugOption(String option, boolean defaultValue) {
		if (debugTracker == null) {
			debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(bundleContext, DebugOptions.class.getName(), null);
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
	public EventAdmin getEventAdmin() {
		if (eventAdminTracker == null) {
			if (bundleContext == null)
				return null;
			eventAdminTracker = new ServiceTracker<EventAdmin, EventAdmin>(bundleContext, EventAdmin.class.getName(), null);
			eventAdminTracker.open();
		}
		return eventAdminTracker.getService();
	}

}
