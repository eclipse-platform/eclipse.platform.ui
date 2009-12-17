/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.services;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.services.IServiceLocator;

/**
 * When creating components this service can be used to create the appropriate
 * service locator for the new component. For use with the component framework.
 * <p>
 * <b>Note:</b> Must not be implemented or extended by clients.
 * <p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.  This might disappear in 3.4 M5.
 * </p>
 * 
 * 
 * @since 3.4
 */
public interface IServiceLocatorCreator {
	/**
	 * Creates a service locator that can be used for hosting a new service
	 * context. It will have the appropriate child services created as needed,
	 * and can be used with the Dependency Injection framework to reuse
	 * components (by simply providing your own implementation for certain
	 * services).
	 * 
	 * @param parent
	 *            the parent locator
	 * @param factory
	 *            a factory that can lazily provide services if requested. This
	 *            may be <code>null</code>
	 * @param owner
	 *            an object whose {@link IDisposable#dispose()} method will be
	 *            called on the UI thread if the created service locator needs
	 *            to be disposed (typically, because a plug-in contributing
	 *            services to the service locator via an
	 *            {@link AbstractServiceFactory} is no longer available). The
	 *            owner can be any object that implements {@link IDisposable}.
	 *            The recommended implementation of the owner's dispose method
	 *            is to do whatever is necessary to stop using the created
	 *            service locator, and then to call
	 *            {@link IDisposable#dispose()} on the service locator.
	 * @return the created service locator. The returned service locator will be
	 *         an instance of {@link IDisposable}.
	 */
	public IServiceLocator createServiceLocator(IServiceLocator parent,
			AbstractServiceFactory factory, IDisposable owner);
}