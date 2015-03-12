/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.services;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {

	public final static String PLUGIN_ID = "org.eclipse.e4.ui.services"; //$NON-NLS-1$

	private static Activator singleton;

	private ServiceRegistration contextServiceReg;
	private ServiceRegistration handlerServiceReg;

	private ServiceTracker eventAdminTracker;
	private BundleContext bundleContext;

	/*
	 * Returns the singleton for this Activator. Callers should be aware that
	 * this will return null if the bundle is not active.
	 */
	public static Activator getDefault() {
		return singleton;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		bundleContext = context;
		singleton = this;
	}

	/*
	 * Return the debug options service, if available.
	 */
	public EventAdmin getEventAdmin() {
		if (eventAdminTracker == null) {
			eventAdminTracker = new ServiceTracker(bundleContext, EventAdmin.class.getName(), null);
			eventAdminTracker.open();
		}
		return (EventAdmin) eventAdminTracker.getService();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (contextServiceReg != null) {
			contextServiceReg.unregister();
			contextServiceReg = null;
		}
		if (handlerServiceReg != null) {
			handlerServiceReg.unregister();
			handlerServiceReg = null;
		}

		if (eventAdminTracker != null) {
			eventAdminTracker.close();
			eventAdminTracker = null;
		}
		bundleContext = null;
		singleton = null;
	}

	public BundleContext getBundleContext() {
		return bundleContext;
	}
}
