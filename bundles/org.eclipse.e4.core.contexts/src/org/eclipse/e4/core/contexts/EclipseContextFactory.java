/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.core.contexts;

import java.util.WeakHashMap;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.e4.core.internal.contexts.osgi.EclipseContextOSGi;
import org.osgi.framework.BundleContext;

/**
 * A factory for creating context instances.
 */
public final class EclipseContextFactory {

	/**
	 * Creates and returns a new empty context.
	 * @return A new empty context.
	 */
	static public IEclipseContext create() {
		return new EclipseContext(null);
	}

	/**
	 * Creates and returns a new empty context.
	 * @return A new empty context.
	 */
	static public IEclipseContext create(String name) {
		IEclipseContext result = create();
		result.set(EclipseContext.DEBUG_STRING, name);
		return result;
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
				result = new EclipseContextOSGi(bundleContext);
				result.set(EclipseContext.DEBUG_STRING, "OSGi context for bundle: " + bundleContext.getBundle().getSymbolicName()); //$NON-NLS-1$
				serviceContexts.put(bundleContext, result);
			}
			return result;
		}
	}
}