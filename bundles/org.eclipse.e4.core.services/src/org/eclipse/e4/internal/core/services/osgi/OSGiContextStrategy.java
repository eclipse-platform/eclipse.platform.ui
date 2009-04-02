/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.internal.core.services.osgi;

import java.util.*;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ILookupStrategy;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * A context strategy that provides access to OSGi services.
 * <p>
 * OSGi services are looked up by service class name.
 */
public class OSGiContextStrategy implements ILookupStrategy, IDisposable, ServiceTrackerCustomizer {
	class ServiceData {
		//the service name
		String name;

		ServiceTracker tracker;
		//the contexts using this service (IEclipseContext -> null)
		final Map users = new WeakHashMap();

		ServiceData(String name) {
			this.name = name;
		}

		public void addContext(IEclipseContext originatingContext) {
			users.put(originatingContext, null);
		}
	}

	private final BundleContext bundleContext;
	/**
	 * Map of String (service name) -> ServiceData
	 */
	private Map services = Collections.synchronizedMap(new HashMap());

	public OSGiContextStrategy(BundleContext bc) {
		super();
		this.bundleContext = bc;
	}

	public Object addingService(ServiceReference reference) {
		String name = serviceName(reference);
		Object newValue = bundleContext.getService(reference);
		if (newValue == null)
			return null;
		//for performance we store the concrete service object with each context that requested it
		ServiceData data = (ServiceData) services.get(name);
		for (Iterator it = data.users.keySet().iterator(); it.hasNext();)
			((IEclipseContext) it.next()).set(name, newValue);
		return newValue;
	}

	/**
	 * Discards any services that are no longer used by any strongly
	 * reachable contexts.
	 */
	private void cleanReferences() {
		synchronized (services) {
			for (Iterator it = services.values().iterator(); it.hasNext();) {
				ServiceData data = (ServiceData) it.next();
				//if there are no more references, discard the service
				if (data.users.isEmpty()) {
					data.tracker.close();
					it.remove();
				}
			}
		}
	}

	public boolean containsKey(String name, IEclipseContext context) {
		cleanReferences();
		return bundleContext.getServiceReference(name) != null;
	}

	public void dispose() {
		synchronized (services) {
			for (Iterator it = services.values().iterator(); it.hasNext();)
				((ServiceData) it.next()).tracker.close();
			services.clear();
		}
	}

	public Object lookup(String name, IEclipseContext originatingContext) {
		cleanReferences();
		ServiceData data = (ServiceData) services.get(name);
		if (data == null) {
			data = new ServiceData(name);
			data.tracker = new ServiceTracker(bundleContext, name, this);
			services.put(name, data);
			//just opening a tracker will cause values to be set by the tracker callback methods
			data.tracker.open();
		}
		data.addContext(originatingContext);
		return data.tracker.getService();
	}

	public void modifiedService(ServiceReference reference, Object service) {
		String name = serviceName(reference);
		ServiceData data = (ServiceData) services.get(name);
		for (Iterator it = data.users.keySet().iterator(); it.hasNext();)
			((IEclipseContext) it.next()).set(name, service);
	}

	public void removedService(ServiceReference reference, Object service) {
		String name = serviceName(reference);
		//must set to null rather than removing so injection continues to work
		ServiceData data = (ServiceData) services.get(name);
		for (Iterator it = data.users.keySet().iterator(); it.hasNext();)
			((IEclipseContext) it.next()).set(name, null);
		bundleContext.ungetService(reference);
	}

	/**
	 * Returns the service name for a service reference
	 */
	private String serviceName(ServiceReference reference) {
		return ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
	}
}
