/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A simple service locator creator.
 *
 * @since 3.4
 */
public class ServiceLocatorCreator implements IServiceLocatorCreator {

	@Override
	public IServiceLocator createServiceLocator(IServiceLocator parent, AbstractServiceFactory factory,
			IDisposable owner) {
		ServiceLocator serviceLocator = new ServiceLocator(parent, factory, owner);
		// System.err.println("parentLocator: " + parent); //$NON-NLS-1$
		if (parent != null) {
			IEclipseContext ctx = parent.getService(IEclipseContext.class);
			if (ctx != null) {
				serviceLocator.setContext(ctx.createChild());
			}
		}
		return serviceLocator;
	}

	@Override
	public IServiceLocator createServiceLocator(IServiceLocator parent, AbstractServiceFactory factory,
			IDisposable owner, IEclipseContext context) {
		ServiceLocator serviceLocator = new ServiceLocator(parent, factory, owner);
		serviceLocator.setContext(context);
		return serviceLocator;
	}
}
