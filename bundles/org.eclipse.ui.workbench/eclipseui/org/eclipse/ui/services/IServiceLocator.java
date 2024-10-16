/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430981 - Generified API
 *******************************************************************************/

package org.eclipse.ui.services;

/**
 * <p>
 * A component with which one or more services are registered. The services can
 * be retrieved from this locator using some key -- typically the class
 * representing the interface the service must implement. For example:
 * </p>
 *
 * <pre>
 * IHandlerService service = workbenchWindow.getService(IHandlerService.class);
 * </pre>
 *
 * <p>
 * This interface is not to be implemented or extended by clients.
 * </p>
 *
 * @since 3.2
 */
public interface IServiceLocator {

	/**
	 * Retrieves the service corresponding to the given API.
	 *
	 * @param api This is the interface that the service implements. Must not be
	 *            <code>null</code>.
	 * @return The service, or <code>null</code> if no such service could be found.
	 */

	<T> T getService(Class<T> api);

	/**
	 * Whether this service exists within the scope of this service locator. This
	 * does not include looking for the service within the scope of the parents.
	 * This method can be used to determine whether a particular service supports
	 * nesting in this scope.
	 *
	 * @param api This is the interface that the service implements. Must not be
	 *            <code>null</code>.
	 * @return <code>true</code> if the service locator can find a service for the
	 *         given API; <code>false</code> otherwise.
	 */
	boolean hasService(Class<?> api);
}
