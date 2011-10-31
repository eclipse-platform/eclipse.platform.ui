/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.core.internal.contexts.osgi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;

public class EclipseContextOSGi extends EclipseContext implements ServiceListener, SynchronousBundleListener {

	final private BundleContext bundleContext;

	private Map<String, ServiceReference<?>> refs = Collections.synchronizedMap(new HashMap<String, ServiceReference<?>>());

	public EclipseContextOSGi(BundleContext bundleContext) {
		super(null);
		this.bundleContext = bundleContext;
		try {
			// process all IContextFunction services already registered
			ServiceReference<?>[] existing = bundleContext.getServiceReferences(IContextFunction.SERVICE_NAME, null);
			if (existing != null) {
				for (int i = 0; i < existing.length; i++) {
					String name = (String) existing[i].getProperty(IContextFunction.SERVICE_CONTEXT_KEY);
					refs.put(name, existing[i]);
					localValues.put(name, bundleContext.getService(existing[i]));
				}
			}
		} catch (InvalidSyntaxException e) {
			// should never happen
		}
		this.bundleContext.addServiceListener(this);
		this.bundleContext.addBundleListener(this);
	}

	public boolean containsKey(String name, boolean localOnly) {
		if (super.containsKey(name, localOnly))
			return true;
		Object result = lookup(name, this);
		return (result != null);
	}

	public Object lookup(String name, EclipseContext originatingContext) {
		if (name == null)
			return null;
		if (refs.containsKey(name)) { // retrieve service again 
			// This could be reached, for instance, if previously stored service value is overridden or removed from the context.
			ServiceReference<?> ref = refs.get(name);
			if (ref == null)
				return null;
			Object service = bundleContext.getService(ref);
			bundleContext.ungetService(ref);
			localValues.put(name, service);
			return service;
		}
		ServiceReference<?> ref = bundleContext.getServiceReference(name);
		if (ref == null) {
			refs.put(name, null);
			return null;
		}
		refs.put(name, ref);
		Object service = bundleContext.getService(ref);
		localValues.put(name, service);
		return service;
	}

	/**
	 * Returns the service name for a service reference
	 */
	private String serviceName(ServiceReference<?> reference) {
		return ((String[]) reference.getProperty(Constants.OBJECTCLASS))[0];
	}

	public void dispose() {
		for (ServiceReference<?> ref : refs.values()) {
			if (ref != null)
				bundleContext.ungetService(ref);
		}
		refs.clear();
		bundleContext.removeServiceListener(this);
		bundleContext.removeBundleListener(this);
		super.dispose();
	}

	public void serviceChanged(ServiceEvent event) {
		ServiceReference<?> ref = event.getServiceReference();
		String name = serviceName(ref);
		if (IContextFunction.SERVICE_NAME.equals(name)) // those keep associated names
			name = (String) ref.getProperty(IContextFunction.SERVICE_CONTEXT_KEY);

		if (refs.containsKey(name)) {
			ServiceReference<?> oldRef = refs.get(name);
			if (oldRef != null)
				bundleContext.ungetService(oldRef);
			if (event.getType() == ServiceEvent.UNREGISTERING) {
				refs.put(name, null);
				remove(name);
			} else {
				Object service = bundleContext.getService(ref);
				refs.put(name, ref);
				set(name, service);
			}
		}
	}

	public void bundleChanged(BundleEvent event) {
		// In case OSGi context has not being properly disposed by the application,
		// OSGi framework shutdown will trigged uninjection of all consumed OSGi 
		// service. To avoid this, we detect framework shutdown and release services.
		if (event.getType() != BundleEvent.STOPPING)
			return;
		if (event.getBundle().getBundleId() == 0)
			dispose();
	}
}
