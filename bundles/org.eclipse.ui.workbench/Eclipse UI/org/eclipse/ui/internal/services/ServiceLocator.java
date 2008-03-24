/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.2
 * 
 */
public final class ServiceLocator implements IDisposable, INestable,
		IServiceLocator {
	boolean activated = false;

	private static class ParentLocator implements IServiceLocator {
		private IServiceLocator locator;
		private Class key;

		public ParentLocator(IServiceLocator parent, Class serviceInterface) {
			locator = parent;
			key = serviceInterface;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.services.IServiceLocator#getService(java.lang.Class)
		 */
		public Object getService(Class api) {
			if (key.equals(api)) {
				return locator.getService(key);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.services.IServiceLocator#hasService(java.lang.Class)
		 */
		public boolean hasService(Class api) {
			if (key.equals(api)) {
				return true;
			}
			return false;
		}
	}

	private AbstractServiceFactory factory;

	/**
	 * The parent for this service locator. If a service can't be found in this
	 * locator, then the parent is asked. This value may be <code>null</code>
	 * if there is no parent.
	 */
	private IServiceLocator parent;

	/**
	 * The map of servicesThis value is <code>null</code> until a service is
	 * registered.
	 */
	private Map services = null;

	/**
	 * Constructs a service locator with no parent.
	 */
	public ServiceLocator() {
		this(null, null);
	}

	/**
	 * Constructs a service locator with the given parent.
	 * 
	 * @param parent
	 *            The parent for this service locator; this value may be
	 *            <code>null</code>.
	 * @param factory
	 *            a local factory that can provide services at this level
	 */
	public ServiceLocator(final IServiceLocator parent,
			AbstractServiceFactory factory) {
		this.parent = parent;
		this.factory = factory;
	}

	public final void activate() {
		activated = true;
		if (services != null) {
			final Iterator serviceItr = services.values().iterator();
			while (serviceItr.hasNext()) {
				final Object service = serviceItr.next();
				if (service instanceof INestable) {
					final INestable nestableService = (INestable) service;
					nestableService.activate();
				}
			}
		}
	}

	public final void deactivate() {
		activated = false;
		if (services != null) {
			final Iterator serviceItr = services.values().iterator();
			while (serviceItr.hasNext()) {
				final Object service = serviceItr.next();
				if (service instanceof INestable) {
					final INestable nestableService = (INestable) service;
					nestableService.deactivate();
				}
			}
		}
	}

	public final void dispose() {
		if (services != null) {
			final Iterator serviceItr = services.values().iterator();
			while (serviceItr.hasNext()) {
				final Object object = serviceItr.next();
				if (object instanceof IDisposable) {
					final IDisposable service = (IDisposable) object;
					service.dispose();
				}
			}
			services = null;
		}
		parent = null;
	}

	public final Object getService(final Class key) {
		Object service;
		if (services != null) {
			service = services.get(key);
		} else {
			service = null;
		}
		if (service == null) {
			// if we don't have a service in our cache then:
			// 1. check our local factory
			// 2. go to the registry
			// or 3. use the parent service
			IServiceLocator factoryParent = WorkbenchServiceRegistry.GLOBAL_PARENT;
			if (parent != null) {
				factoryParent = new ParentLocator(parent, key);
			}
			if (factory != null) {
				service = factory.create(key, factoryParent, this);
			}
			if (service == null) {
				service = WorkbenchServiceRegistry.getRegistry().getService(
						key, factoryParent, this);
			}
			if (service == null) {
				service = factoryParent.getService(key);
			} else {
				registerService(key, service);
			}
		}
		return service;
	}

	public final boolean hasService(final Class key) {
		if (services != null) {
			if (services.containsKey(key)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Registers a service with this locator. If there is an existing service
	 * matching the same <code>api</code> and it implements
	 * {@link IDisposable}, it will be disposed.
	 * 
	 * @param api
	 *            This is the interface that the service implements. Must not be
	 *            <code>null</code>.
	 * @param service
	 *            The service to register. This must be some implementation of
	 *            <code>api</code>. This value must not be <code>null</code>.
	 */
	public final void registerService(final Class api, final Object service) {
		if (api == null) {
			throw new NullPointerException("The service key cannot be null"); //$NON-NLS-1$
		}

		if (!api.isInstance(service)) {
			throw new IllegalArgumentException(
					"The service does not implement the given interface"); //$NON-NLS-1$
		}

		if (services == null) {
			services = new HashMap();
		}

		if (services.containsKey(api)) {
			final Object currentService = services.remove(api);
			if (currentService instanceof IDisposable) {
				final IDisposable disposable = (IDisposable) currentService;
				disposable.dispose();
			}
		}

		if (service == null) {
			if (services.isEmpty()) {
				services = null;
			}
		} else {
			services.put(api, service);
			if (service instanceof INestable && activated) {
				((INestable)service).activate();
			}
		}
	}

}
