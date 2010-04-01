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
package org.eclipse.e4.internal.core.services.osgi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.IContextFunction;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.context.spi.ILookupStrategy;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * A context strategy that provides access to OSGi services.
 * <p>
 * OSGi services are looked up by service class name.
 */
public class OSGiContextStrategy implements ILookupStrategy, IDisposable, ServiceTrackerCustomizer,
		IRunAndTrack {
	class ServiceData {
		// the service name
		String name;

		ServiceTracker tracker;
		// the contexts using this service (IEclipseContext -> null)
		final Map<IEclipseContext, Object> users = Collections
				.synchronizedMap(new WeakHashMap<IEclipseContext, Object>());

		ServiceData(String name) {
			this.name = name;
		}

		public void addContext(IEclipseContext originatingContext) {
			users.put(originatingContext, null);
			// track this context so we can cleanup when the context is disposed
			originatingContext.runAndTrack(OSGiContextStrategy.this, null);
		}

		public IEclipseContext[] getUsingContexts() {
			return users.keySet().toArray(new IEclipseContext[users.size()]);
		}
	}

	/**
	 * Maintains a cache of registered context functions, indexed by context function key.
	 */
	class ContextFunctionCache implements ServiceListener {
		final Map<String, ServiceReference> functionKeys = Collections
				.synchronizedMap(new HashMap<String, ServiceReference>());

		public ContextFunctionCache() {
			try {
				String filter = "(" + Constants.OBJECTCLASS + '=' + IContextFunction.SERVICE_NAME //$NON-NLS-1$
						+ ')';
				bundleContext.addServiceListener(this, filter);
				// process all services already registered
				ServiceReference[] existing = bundleContext.getServiceReferences(
						IContextFunction.SERVICE_NAME, null);
				for (int i = 0; i < existing.length; i++)
					add(existing[i]);
			} catch (InvalidSyntaxException e) {
				// should never happen
				throw new RuntimeException(e);
			}

		}

		/**
		 * Process an added service reference to a context function.
		 */
		private void add(ServiceReference ref) {
			String key = (String) ref.getProperty(IContextFunction.SERVICE_CONTEXT_KEY);
			if (key != null)
				functionKeys.put(key, ref);
		}

		public ServiceReference lookup(String key) {
			return functionKeys.get(key);
		}

		public void dispose() {
			bundleContext.removeServiceListener(this);
			functionKeys.clear();
		}

		/**
		 * Process a removed service reference from a context function.
		 */
		private void remove(ServiceReference ref) {
			String key = (String) ref.getProperty(IContextFunction.SERVICE_CONTEXT_KEY);
			if (key != null)
				functionKeys.remove(key);
		}

		public void serviceChanged(ServiceEvent event) {
			switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				add(event.getServiceReference());
				break;
			case ServiceEvent.UNREGISTERING:
				remove(event.getServiceReference());
				break;
			}
		}
	}

	final BundleContext bundleContext;

	private ContextFunctionCache functionCache;
	/**
	 * Map of String (service name) -> ServiceData
	 */
	private Map<String, ServiceData> services = Collections
			.synchronizedMap(new HashMap<String, ServiceData>());

	public OSGiContextStrategy(BundleContext bc) {
		super();
		this.bundleContext = bc;
		functionCache = new ContextFunctionCache();
	}

	public Object addingService(ServiceReference reference) {
		String name = serviceName(reference);
		Object newValue = bundleContext.getService(reference);
		if (newValue == null)
			return null;
		// for performance we store the concrete service object with each context that requested it
		ServiceData data = getServiceData(name);
		// may have been cleaned up concurrently
		if (data == null)
			return null;
		for (IEclipseContext user : data.getUsingContexts())
			user.set(name, newValue);
		return newValue;
	}

	public boolean containsKey(String name, IEclipseContext context) {
		// first look for a registered IContextFunction matching the name
		if (getContextFunction(name) != null)
			return true;
		// next, look for a matching service
		return bundleContext.getServiceReference(name) != null;
	}

	public void dispose() {
		synchronized (services) {
			for (Iterator<ServiceData> it = services.values().iterator(); it.hasNext();)
				it.next().tracker.close();
			services.clear();
		}
		functionCache.dispose();
	}

	/**
	 * Returns the service data corresponding to the given name, or <code>null</code> if no such
	 * data is available.
	 */
	private ServiceData getServiceData(String name) {
		ServiceData data = services.get(name);
		if (data == null)
			return null;
		if (data.users.isEmpty()) {
			data.tracker.close();
			services.remove(name);
			return null;
		}
		return data;
	}

	public Object lookup(String name, IEclipseContext originatingContext) {
		if (name == null)
			return null;
		ServiceData data = getServiceData(name);
		if (data == null) {
			// first look for a registered IContextFunction matching the name
			ServiceReference ref = getContextFunction(name);
			if (ref != null)
				return bundleContext.getService(ref);
			// services must be fully qualified type names
			if (name.indexOf('.') == -1)
				return null;
			// create a tracker to retrieve the service with the given name
			data = new ServiceData(name);
			try {
				data.tracker = new ServiceTracker(bundleContext, name, this);
			} catch (IllegalArgumentException iae) {
				// we get these when the variables requested are not valid names
				return null;
			}
			// add the context immediately so cleanReferences doesn't remove it
			data.addContext(originatingContext);
			services.put(name, data);
			// just opening a tracker will cause values to be set by the tracker
			// callback methods
			data.tracker.open();
		} else {
			data.addContext(originatingContext);
		}
		return data.tracker.getService();
	}

	/**
	 * Returns an IContextFunction service that computes values for the given name, or
	 * <code>null</code> if there is no matching service.
	 */
	private ServiceReference getContextFunction(String name) {
		return functionCache.lookup(name);
	}

	public void modifiedService(ServiceReference reference, Object service) {
		String name = serviceName(reference);
		ServiceData data = getServiceData(name);
		// may have been cleaned up concurrently
		if (data == null)
			return;
		for (IEclipseContext user : data.getUsingContexts())
			user.set(name, service);
	}

	public void removedService(ServiceReference reference, Object service) {
		String name = serviceName(reference);
		// must set to null rather than removing so injection continues to work
		ServiceData data = getServiceData(name);
		// may have been cleaned up concurrently
		if (data != null) {
			for (IEclipseContext user : data.getUsingContexts())
				user.set(name, null);
		}
		bundleContext.ungetService(reference);
	}

	/**
	 * Returns the service name for a service reference
	 */
	private String serviceName(ServiceReference reference) {
		return ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
	}

	/**
	 * Listen for changes on all contexts that have obtained a service from this strategy, so that
	 * we can do appropriate cleanup of our caches when the requesting context is disposed.
	 */
	public boolean notify(ContextChangeEvent event) {
		IEclipseContext context = event.getContext();
		if (context == null)
			return false;
		if (event.getEventType() != ContextChangeEvent.DISPOSE) {
			// do a lookup so the listener isn't removed
			context.get(IContextConstants.PARENT);
			return true;
		}
		synchronized (services) {
			for (Iterator<ServiceData> it = services.values().iterator(); it.hasNext();) {
				ServiceData data = it.next();
				data.users.remove(context);
				// if there are no more references, discard the service
				if (data.users.isEmpty()) {
					it.remove();
					data.tracker.close();
				}
			}
		}
		return true;
	}
}
