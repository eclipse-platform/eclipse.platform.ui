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
package org.eclipse.e4.core.di.internal.extensions;

import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class DIEActivator implements BundleActivator {

	static private DIEActivator defaultInstance;

	private BundleContext bundleContext;

	private ServiceTracker preferencesTracker;
	private ServiceTracker eventAdminTracker;

	public DIEActivator() {
		defaultInstance = this;
	}

	public static DIEActivator getDefault() {
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
			preferencesTracker = new ServiceTracker(bundleContext, IPreferencesService.class
					.getName(), null);
			preferencesTracker.open();
		}
		return (IPreferencesService) preferencesTracker.getService();
	}

	public EventAdmin getEventAdmin() {
		if (eventAdminTracker == null) {
			if (bundleContext == null)
				return null;
			eventAdminTracker = new ServiceTracker(bundleContext, EventAdmin.class.getName(), null);
			eventAdminTracker.open();
		}
		return (EventAdmin) eventAdminTracker.getService();
	}

}
