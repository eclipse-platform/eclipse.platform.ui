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
	 * create a service locator that can then be used as a site. It will have
	 * the appropriate child services created as needed, and can be used with
	 * the Dependency Injection framework to reuse components (by simply
	 * providing your own implementation for certain services).
	 * 
	 * @param parent
	 *            the parent locator
	 * @param factory
	 *            a factory that can lazily provide services if requested. This
	 *            may be <code>null</code>
	 * @return the created service locator
	 */
	public IServiceLocator createServiceLocator(IServiceLocator parent,
			AbstractServiceFactory factory);
}
