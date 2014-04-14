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

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class DIEActivator implements BundleActivator {

	static private DIEActivator defaultInstance;

	private BundleContext bundleContext;

	private ServiceTracker<IPreferencesService, IPreferencesService> preferencesTracker;
	private ServiceTracker<EventAdmin, EventAdmin> eventAdminTracker;

	private Set<PreferencesObjectSupplier> preferenceSuppliers = new HashSet<PreferencesObjectSupplier>();

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
		for (PreferencesObjectSupplier supplier : preferenceSuppliers) {
			supplier.removeAllListeners();
		}
		preferenceSuppliers.clear();

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
			preferencesTracker = new ServiceTracker<IPreferencesService, IPreferencesService>(bundleContext, IPreferencesService.class, null);
			preferencesTracker.open();
		}
		return preferencesTracker.getService();
	}

	public EventAdmin getEventAdmin() {
		if (eventAdminTracker == null) {
			if (bundleContext == null)
				return null;
			eventAdminTracker = new ServiceTracker<EventAdmin, EventAdmin>(bundleContext, EventAdmin.class, null);
			eventAdminTracker.open();
		}
		return eventAdminTracker.getService();
	}

	public void registerPreferencesSupplier(PreferencesObjectSupplier supplier) {
		preferenceSuppliers.add(supplier);
	}

}
