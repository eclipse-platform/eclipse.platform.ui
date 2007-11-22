/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.services;

/**
 * A factory for creating services for use with the
 * <code>org.eclipse.ui.services</code> extension point. You are given a
 * service locator to look up other services, and can retrieve your parent
 * service (if one has already been created).
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as part
 * of a work in progress. There is a guarantee neither that this API will work
 * nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team ... i.e. you can use it, but stay in
 * touch.
 * </p>
 * 
 * @since 3.4
 */
public abstract class AbstractServiceFactory {

	/**
	 * When a service locator cannot find a service it will request one from the
	 * registry, which will call this factory create method.
	 * <p>
	 * You can use the locator to get any needed services and a parent service
	 * locator will be provided if you need access to the parent service. If the
	 * parent object return from the parent locator is not <code>null</code>
	 * it can be cast to the service interface that is requested. The parent
	 * service locator will only return the serviceInterface service.
	 * </p>
	 * 
	 * @param serviceInterface
	 *            the service we need to create. Will not be <code>null</code>.
	 * @param parentLocator
	 *            A locator that can return a parent service instance if
	 *            desired. The parent service can be cast to serviceInterface.
	 *            Will not be <code>null</code>.
	 * @param locator
	 *            the service locator which can be used to retrieve dependent
	 *            services. Will not be <code>null</code>
	 * @return the created service or <code>null</code>
	 */
	public abstract Object create(Class serviceInterface,
			IServiceLocator parentLocator, IServiceLocator locator);
}
