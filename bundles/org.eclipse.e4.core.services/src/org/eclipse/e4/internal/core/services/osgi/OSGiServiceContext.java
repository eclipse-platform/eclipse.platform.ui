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
import org.eclipse.e4.core.services.context.spi.IContextConstants;
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
	private final ServiceTracker aliasRegistryTracker;
	/**
	 * Map of String (service name) -> ServiceData
	 */
	private Map services = Collections.synchronizedMap(new HashMap());

	public OSGiServiceContext(BundleContext bc) {
		super(null, null);
		this.bundleContext = bc;
		this.aliasRegistryTracker = new ServiceTracker(bc, IServiceAliasRegistry.SERVICE_NAME, null);
		set(IContextConstants.DEBUG_STRING, "OSGi context for bundle: " + bc.getBundle().getSymbolicName()); //$NON-NLS-1$
		aliasRegistryTracker.open();
	}

	public Object addingService(ServiceReference reference) {
		String name = serviceName(reference);
		String alias = unresolve(name);
		Object newValue = bundleContext.getService(reference);
		if (newValue == null)
			return null;
		//for performance we store the concrete service object with each context that requested it
		ServiceData data = (ServiceData) services.get(alias);
		for (Iterator it = data.users.keySet().iterator(); it.hasNext();)
			((IEclipseContext) it.next()).set(alias, newValue);
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
		String unresolved = unresolve(name);
		String resolved = resolve(name);
		return super.containsKey(unresolved) || bundleContext.getServiceReference(resolved) != null;
	}

	public void dispose() {
		synchronized (services) {
			for (Iterator it = services.values().iterator(); it.hasNext();)
				((ServiceData) it.next()).tracker.close();
			services.clear();
		}
		aliasRegistryTracker.close();
	}

	protected Object internalGet(EclipseContext originatingContext, String name, Object[] arguments, boolean local) {
		cleanReferences();
		String alias = unresolve(name);
		String resolved = resolve(name);
		//first see if we have already stored a value locally
		Object result = super.internalGet(originatingContext, alias, arguments, local);
		if (result != null)
			return result;
		ServiceData data = (ServiceData) services.get(alias);
		if (data == null) {
			data = new ServiceData(alias);
			data.tracker = new ServiceTracker(bundleContext, resolved, this);
			services.put(alias, data);
			//just opening a tracker will cause values to be set by the tracker callback methods
			data.tracker.open();
		}
		data.addContext(originatingContext);
		return data.tracker.getService();
	}

	public void modifiedService(ServiceReference reference, Object service) {
		String name = serviceName(reference);
		String alias = unresolve(name);
		ServiceData data = (ServiceData) services.get(alias);
		for (Iterator it = data.users.keySet().iterator(); it.hasNext();)
			((IEclipseContext) it.next()).set(alias, service);
	}

	public void removedService(ServiceReference reference, Object service) {
		String name = serviceName(reference);
		String alias = unresolve(name);
		//must set to null rather than removing so injection continues to work
		ServiceData data = (ServiceData) services.get(alias);
		for (Iterator it = data.users.keySet().iterator(); it.hasNext();)
			((IEclipseContext) it.next()).set(alias, null);
		bundleContext.ungetService(reference);
	}

	/**
	 * Returns the service alias for a resolved service name.
	 * @param serviceName The resolved service name
	 * @return The service alias
	 */
	private String unresolve(String serviceName) {
		IServiceAliasRegistry registry = (IServiceAliasRegistry) aliasRegistryTracker.getService();
		return registry == null ? serviceName : registry.findAlias(serviceName);
	}

	/**
	 * Returns the service alias for a qualified name
	 */
	private String resolve(String alias) {
		IServiceAliasRegistry registry = (IServiceAliasRegistry) aliasRegistryTracker.getService();
		return registry == null ? alias : registry.resolveAlias(alias);
	}

	/**
	 * Returns the service alias for a service reference
	 */
	private String serviceName(ServiceReference reference) {
		return ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
	}
}
