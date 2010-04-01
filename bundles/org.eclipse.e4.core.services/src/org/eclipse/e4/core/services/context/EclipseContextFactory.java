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

package org.eclipse.e4.core.services.context;

import java.util.WeakHashMap;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.context.spi.IEclipseContextStrategy;
import org.eclipse.e4.core.services.context.spi.ILookupStrategy;
import org.eclipse.e4.core.services.internal.context.EclipseContext;
import org.eclipse.e4.internal.core.services.osgi.OSGiContextStrategy;
import org.osgi.framework.BundleContext;

/**
 * A factory for creating a simple context instance. Simple contexts must be filled in
 * programmatically by calling {@link IEclipseContext#set(String, Object)} to provide context
 * values, or by providing an {@link ILookupStrategy} to be used to initialize values not currently
 * defined in the context.
 */
public final class EclipseContextFactory {

	/**
	 * Creates and returns a new empty context with no parent, using the default context strategy.
	 * 
	 * @return A new empty context with no parent context.
	 */
	static public IEclipseContext create() {
		return create(null, null);
	}

	/**
	 * Creates and returns a new empty context with the given parent and strategy.
	 * 
	 * @param parent
	 *            The context parent to delegate lookup of values not defined in the returned
	 *            context.
	 * @param strategy
	 *            The context strategy to use in this context
	 * @return A new empty context with the given parent and strategy
	 */
	static public IEclipseContext create(IEclipseContext parent, IEclipseContextStrategy strategy) {
		EclipseContext eclipseContext = new EclipseContext(parent, strategy);
		eclipseContext.set(IEclipseContext.class.getName(), eclipseContext);
		return eclipseContext;
	}

	private static WeakHashMap<BundleContext, IEclipseContext> serviceContexts = new WeakHashMap<BundleContext, IEclipseContext>();

	/**
	 * Returns a context that can be used to lookup OSGi services. A client must never dispose the
	 * provided context, because it may be shared by multiple callers.
	 * 
	 * @param bundleContext
	 *            The bundle context to use for service lookup
	 * @return A context containing all OSGi services
	 */
	public static IEclipseContext getServiceContext(BundleContext bundleContext) {
		synchronized (serviceContexts) {
			IEclipseContext result = serviceContexts.get(bundleContext);
			if (result == null) {
				result = create(null, new OSGiContextStrategy(bundleContext));
				result.set(IContextConstants.DEBUG_STRING,
						"OSGi context for bundle: " + bundleContext.getBundle().getSymbolicName()); //$NON-NLS-1$
				serviceContexts.put(bundleContext, result);
			}
			return result;
		}
	}

	/**
	 * Creates a new context change event.
	 * 
	 * @noreference This method is not intended to be referenced by clients.
	 * @param context
	 *            The context in which the event occurred
	 * @param eventType
	 *            The type of change that occurred
	 * @param args
	 *            The arguments that were supplied when the context listener was registered
	 * @param name
	 *            The name of the context value that changed
	 * @param oldValue
	 *            The value associated with the changed name before the change occurred. Return
	 *            <code>null</code> if there was no previous value, or if not applicable for this
	 *            type of event.
	 * @return A new context change event
	 */
	public static ContextChangeEvent createContextEvent(IEclipseContext context, int eventType,
			Object[] args, String name, Object oldValue) {
		return new ContextChangeEvent(context, eventType, args, name, oldValue);
	}
}