/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.ArrayList;
import java.util.Iterator;
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

	// private AbstractServiceFactory factory;

	/**
	 * The parent for this service locator. If a service can't be found in this
	 * locator, then the parent is asked. This value may be <code>null</code> if
	 * there is no parent.
	 */
	// private IServiceLocator parent;

	private boolean disposed;

	private final IDisposable owner;

	private IEclipseContext e4Context;

	private ArrayList servicesToDispose = new ArrayList();

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
		// this.parent = parent;
		// this.factory = factory;
		this.owner = owner;
	}

	public final void activate() {
		activated = true;
	}

	public final void deactivate() {
		activated = false;
	}

	public final void dispose() {
		Iterator i = servicesToDispose.iterator();
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

	public final Object getService(final Class key) {
		if (disposed) {
			return null;
		}
		return e4Context.get(key.getName());
	}

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
		if (api == null) {
			throw new NullPointerException("The service key cannot be null"); //$NON-NLS-1$
		}

		if (!api.isInstance(service)) {
			throw new IllegalArgumentException(
					"The service does not implement the given interface"); //$NON-NLS-1$
		}
		servicesToDispose.add(service);
		e4Context.set(api.getName(), service);
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
