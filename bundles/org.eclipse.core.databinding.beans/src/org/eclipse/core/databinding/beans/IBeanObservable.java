/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 147515
 ******************************************************************************/

package org.eclipse.core.databinding.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.observable.IObserving;

/**
 * Provides access to details of bean observables.
 * <p>
 * This interface is not meant to be implemented by clients.
 * </p>
 * 
 * @since 3.3
 */
public interface IBeanObservable extends IObserving {
	/**
	 * @return property descriptor of the property being observed,
	 *         <code>null</code> if the runtime time information was not
	 *         provided on construction of the observable
	 */
	public PropertyDescriptor getPropertyDescriptor();
}
