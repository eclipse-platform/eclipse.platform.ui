/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 436225
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *     Fabio Zadrozny <fabiofz at gmail dot com> - Bug 459833
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
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
		private Class<?> key;

		public ParentLocator(IServiceLocator parent, Class<?> serviceInterface) {
			locator = parent;
			key = serviceInterface;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getService(Class<T> api) {
			if (key.equals(api)) {
				return (T) locator.getService(key);
			}
			return null;
		}

		@Override
		public boolean hasService(Class<?> api) {
			if (key.equals(api)) {
				return true;
			}
			return false;
		}
	}

	private final AbstractServiceFactory factory;

	/**
	 * The parent for this service locator. If a service can't be found in this
	 * locator, then the parent is asked. This value may be <code>null</code> if
	 * there is no parent.
	 */
	private final IServiceLocator parent;

	private volatile boolean disposed;

	private IDisposable owner;

	private volatile IEclipseContext e4Context;

	private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

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

		for (Object service : services.values()) {
			if (!(service instanceof INestable)) {
				continue;
			}
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					((INestable) service).activate();
				}

				@Override
				public void handleException(Throwable ex) {
					WorkbenchPlugin.log(StatusUtil.newStatus(IStatus.ERROR, "Error while activating: " + service, ex)); //$NON-NLS-1$
				}
			});
		}
	}

	@Override
	public final void deactivate() {
		activated = false;

		for (Object service : services.values()) {
			if (!(service instanceof INestable)) {
				continue;
			}
			SafeRunner.run(new ISafeRunnable() {
				@Override
				public void run() throws Exception {
					((INestable) service).deactivate();
				}

				@Override
				public void handleException(Throwable ex) {
					WorkbenchPlugin
							.log(StatusUtil.newStatus(IStatus.ERROR, "Error while deactivating: " + service, ex)); //$NON-NLS-1$
				}
			});
		}
	}

	@Override
	public final void dispose() {
		disposeServices();
		if (services.size() > 0) {
			// If someone registered during shutdown, dispose of it too.
			// See: Bug 459833 - ConcurrentModificationException in
			// ServiceLocator.dispose
			disposeServices();
		}
		// Check if there was some other leftover and warn about it.
		if (services.size() > 0) {
			WorkbenchPlugin.log(StatusUtil.newStatus(IStatus.WARNING,
					String.format(
							"Services: %s register themselves while disposing (skipping dispose of such services).", //$NON-NLS-1$
							services),
					null));
		}
		services.clear();
		disposed = true;
		e4Context = null;
		owner = null;
	}

	private void disposeServices() {
		Iterator<Entry<Class<?>, Object>> iterator = services.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Class<?>, Object> entry = iterator.next();
			if (entry.getValue() instanceof IDisposable) {
				IDisposable iDisposable = (IDisposable) entry.getValue();
				SafeRunner.run(new ISafeRunnable() {
					@Override
					public void run() throws Exception {
						iDisposable.dispose();
					}

					@Override
					public void handleException(Throwable ex) {
						WorkbenchPlugin
								.log(StatusUtil.newStatus(IStatus.ERROR,
										"Error while disposing: " + iDisposable.getClass().getName(), ex)); //$NON-NLS-1$
					}
				});
			}
			iterator.remove();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <T> T getService(final Class<T> key) {
		IEclipseContext context = e4Context;
		if (context == null) {
			return null;
		}
		if (IEclipseContext.class.equals(key)) {
			return (T) context;
		}

		Object service = context.get(key.getName());
		if (service == null) {
			// this scenario can happen when we dispose the service locator
			// after the window has been removed, in that case the window's
			// context has been destroyed so we should check our own local cache
			// of services first before checking the registry
			service = services.get(key);
		} else if (service == context.getLocal(key.getName())) {
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
		return (T) service;
	}

	@Override
	public final boolean hasService(final Class<?> key) {
		IEclipseContext context = e4Context;
		if (context == null) {
			return false;
		}
		return context.containsKey(key.getName());
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
		if (isDisposed()) {
			IllegalStateException ex = new IllegalStateException("An attempt was made to register service " + service //$NON-NLS-1$
					+ " with implementation class " + api + " on a disposed service locator"); //$NON-NLS-1$//$NON-NLS-2$
			WorkbenchPlugin.log(StatusUtil.newStatus(IStatus.ERROR, ex.getMessage(), ex));
			return;
		}

		if (service instanceof INestable && activated) {
			((INestable) service).activate();
		}

		services.put(api, service);

		if (saveInContext) {
			IEclipseContext context = e4Context;
			if (context == null) {
				return;
			}
			context.set(api.getName(), service);
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
