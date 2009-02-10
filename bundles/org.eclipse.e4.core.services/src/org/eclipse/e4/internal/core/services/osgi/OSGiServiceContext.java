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
import org.eclipse.e4.core.services.internal.context.EclipseContext;
import org.eclipse.e4.core.services.osgi.IServiceAliasRegistry;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * A context that provides access to OSGi services.
 * <p>
 * OSGi services can either be looked up by class name, or using a service alias
 * registered with an {@link IServiceAliasRegistry}.
 */
public class OSGiServiceContext extends EclipseContext implements IDisposable, ServiceTrackerCustomizer {
	class ServiceData {
		//the service name
		String name;

		ServiceTracker tracker;
		//the contexts using this service (IEclipseContext -> null)
		final Map users = new WeakHashMap();

		ServiceData(String name) {
			this.name = name;
		}

		public void addContext(EclipseContext originatingContext) {
			users.put(originatingContext, null);
		}
	}

	private final BundleContext bundleContext;
	/**
	 * Map of String (service name) -> ServiceData
	 */
	private Map services = Collections.synchronizedMap(new HashMap());

	public OSGiServiceContext(BundleContext bc) {
		super(null, "OSGi context for bundle: " + bc.getBundle().getSymbolicName(), null);
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
					remove(data.name);
				}
			}
		}
	}

	public boolean containsKey(String name) {
		cleanReferences();
		String resolved = resolve(name);
		return super.containsKey(resolved) || bundleContext.getServiceReference(resolved) != null;
	}

	public void dispose() {
		synchronized (services) {
			for (Iterator it = services.values().iterator(); it.hasNext();)
				((ServiceData) it.next()).tracker.close();
			services.clear();
		}
	}

	protected Object internalGet(EclipseContext originatingContext, String name, Object[] arguments, boolean local) {
		cleanReferences();
		String resolved = resolve(name);
		//first see if we have already stored a value locally
		Object result = super.internalGet(originatingContext, resolved, arguments, local);
		if (result != null)
			return result;
		ServiceData data = (ServiceData) services.get(resolved);
		if (data == null) {
			data = new ServiceData(resolved);
			data.tracker = new ServiceTracker(bundleContext, resolved, this);
			services.put(resolved, data);
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
	 * Returns the service alias for a qualified name
	 */
	private String resolve(String alias) {
		//getting the alias registry to resolve itself will cause infinite recursion
		if (alias == IServiceAliasRegistry.SERVICE_NAME)
			return alias;
		IServiceAliasRegistry service = (IServiceAliasRegistry) get(IServiceAliasRegistry.SERVICE_NAME);
		return service == null ? alias : service.resolveAlias(alias);
	}

	/**
	 * Returns the service alias for a service reference
	 */
	private String serviceName(ServiceReference reference) {
		return ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
	}
}
