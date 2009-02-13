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

package org.eclipse.e4.core.services.context;

import org.eclipse.e4.core.services.context.spi.IEclipseContextStrategy;
import org.eclipse.e4.core.services.internal.context.EclipseContext;
import org.eclipse.e4.internal.core.services.osgi.OSGiServiceContext;
import org.osgi.framework.BundleContext;

/**
 * A factory for creating a simple context instance. Simple contexts must be
 * filled in programmatically by calling {@link IEclipseContext#set(String, Object)}
 * to provide context values.
 */
public final class EclipseContextFactory {

	/**
	 * Creates and returns a new empty context with no parent, using
	 * the default context strategy.
	 * 
	 * TODO do we need a name?
	 * @return A new empty context with no parent context.
	 */
	static public IEclipseContext create() {
		return new EclipseContext(null, null);
	}

	static public IEclipseContext create(IEclipseContext parent, IEclipseContextStrategy strategy) {
		return new EclipseContext(parent, strategy);
	}

	/**
	 * Returns a context that can be used to lookup OSGi services.
	 * @param bundleContext The  bundle context to use for service lookup
	 * @return A context containing all OSGi services
	 */
	public static IEclipseContext createServiceContext(BundleContext bundleContext) {
		return new OSGiServiceContext(bundleContext);
	}
}