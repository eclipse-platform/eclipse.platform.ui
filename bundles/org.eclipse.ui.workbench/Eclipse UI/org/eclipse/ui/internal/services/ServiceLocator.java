/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
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
import org.eclipse.e4.core.contexts.IEclipseContext;
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
		 * @see
		 * org.eclipse.ui.services.IServiceLocator#getService(java.lang.Class)
		 */
		@Override
		public Object getService(Class api) {
			if (key.equals(api)) {
				return locator.getService(key);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ui.services.IServiceLocator#hasService(java.lang.Class)
		 */
		@Override
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
	 * locator, then the parent is asked. This value may be <code>null</code> if
	 * there is no parent.
	 */
	private IServiceLocator parent;

	private boolean disposed;

	private final IDisposable owner;

	private IEclipseContext e4Context;

	private Map<Class<?>, Object> servicesToDispose = new HashMap<Class<?>, Object>();

	/**
	 * Constructs a service locator with no parent.
	 */
	public ServiceLocator() {
		this(null, null, null);
	}

	/**
	 * Constructs a service locator with the given parent.
	 * 
	 * @param parent
	 *            The parent for this service locator; this value may be
	 *            <code>null</code>.
	 * @param factory
	 *            a local factory that can provide services at this level
	 * @param owner
	 */
	public ServiceLocator(final IServiceLocator parent,
			AbstractServiceFactory factory, IDisposable owner) {
		this.parent = parent;
		this.factory = factory;
		this.owner = owner;
	}

	@Override
	public final void activate() {
		activated = true;

		for (Object service : servicesToDispose.values()) {
			if (service instanceof INestable) {
				((INestable) service).activate();
			}
		}
	}

	@Override
	public final void deactivate() {
		activated = false;

		for (Object service : servicesToDispose.values()) {
			if (service instanceof INestable) {
				((INestable) service).deactivate();
			}
		}
	}

	@Override
	public final void dispose() {
		Iterator<Object> i = servicesToDispose.values().iterator();
		while (i.hasNext()) {
			Object obj = i.next();
			if (obj instanceof IDisposable) {
				((IDisposable) obj).dispose();
			}
		}
		servicesToDispose.clear();
		e4Context = null;
		disposed = true;
	}

	@Override
	public final Object getService(final Class key) {
		if (disposed) {
			return null;
		} else if (IEclipseContext.class.equals(key)) {
			return e4Context;
		}

		Object service = e4Context.get(key.getName());
		if (service == null) {
			// this scenario can happen when we dispose the service locator
			// after the window has been removed, in that case the window's
			// context has been destroyed so we should check our own local cache
			// of services first before checking the registry
			service = servicesToDispose.get(key);
		} else if (service == e4Context.getLocal(key.getName())) {
			// store this service retrieved from the context in the map only if
			// it is a local service for this context, as otherwise we do not
			// want to dispose it when this service locator gets disposed
			registerService(key, service, false);
		}

		if (service == null) {
			// nothing cached, check registry then parent
			IServiceLocator factoryParent = WorkbenchServiceRegistry.GLOBAL_PARENT;
			if (parent != null) {
				factoryParent = new ParentLocator(parent, key);
			}
			if (factory != null) {
				service = factory.create(key, factoryParent, this);
			}
			if (service == null) {
				service = WorkbenchServiceRegistry.getRegistry().getService(key, factoryParent,
						this);
			}
			if (service == null) {
				service = factoryParent.getService(key);
			} else {
				registerService(key, service, true);
			}
		}
		return service;
	}

	@Override
	public final boolean hasService(final Class key) {
		if (disposed) {
			return false;
		}
		return e4Context.containsKey(key.getName());
	}

	/**
	 * Registers a service with this locator. If there is an existing service
	 * matching the same <code>api</code> and it implements {@link IDisposable},
	 * it will be disposed.
	 * 
	 * @param api
	 *            This is the interface that the service implements. Must not be
	 *            <code>null</code>.
	 * @param service
	 *            The service to register. This must be some implementation of
	 *            <code>api</code>. This value must not be <code>null</code>.
	 */
	public final void registerService(final Class api, final Object service) {
		registerService(api, service, true);
	}

	private void registerService(Class<?> api, Object service, boolean saveInContext) {
		if (api == null) {
			throw new NullPointerException("The service key cannot be null"); //$NON-NLS-1$
		}

		if (!api.isInstance(service)) {
			throw new IllegalArgumentException(
					"The service does not implement the given interface"); //$NON-NLS-1$
		}

		if (service instanceof INestable && activated) {
			((INestable) service).activate();
		}

		servicesToDispose.put(api, service);

		if (saveInContext) {
			e4Context.set(api.getName(), service);
		}
	}

	/**
	 * @return
	 */
	public boolean isDisposed() {
		return disposed;
	}

	/**
	 * Some services that were contributed to this locator are no longer
	 * available (because the plug-in containing the AbstractServiceFactory is
	 * no longer available). Notify the owner of the locator about this.
	 */
	public void unregisterServices(String[] serviceNames) {
		if (owner != null) {
			owner.dispose();
		}
	}

	public void setContext(IEclipseContext context) {
		e4Context = context;
	}

	public IEclipseContext getContext() {
		return e4Context;
	}
}
