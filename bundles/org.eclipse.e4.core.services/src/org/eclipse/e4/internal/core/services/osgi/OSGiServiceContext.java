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

import java.lang.ref.WeakReference;
import java.util.*;
import org.eclipse.e4.core.services.IDisposable;
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
	static class ServiceData {
		String name;

		ServiceTracker tracker;
		//the contexts using this service
		final Set users = new HashSet();

		ServiceData(String name) {
			this.name = name;
		}

		public void addContext(EclipseContext originatingContext) {
			users.add(new WeakReference(originatingContext));
		}
	}

	private BundleContext bundleContext;
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
		if (newValue != null)
			set(name, newValue);
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
				//remove any reference whose referent is gone
				for (Iterator it2 = data.users.iterator(); it2.hasNext();) {
					WeakReference ref = (WeakReference) it2.next();
					if (ref.get() == null)
						it2.remove();
				}
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
		return super.internalGet(originatingContext, resolved, arguments, local);
	}

	public void modifiedService(ServiceReference reference, Object service) {
		String name = serviceName(reference);
		set(name, service);
	}

	public void removedService(ServiceReference reference, Object service) {
		String name = serviceName(reference);
		//must set to null rather than removing so injection continues to work
		set(name, null);
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
