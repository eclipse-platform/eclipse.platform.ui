/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.beans;

import java.beans.PropertyDescriptor;

/**
 * Provides access to details of bean observables.
 * <p>
 * This interface is not meant to be implemented by clients.
 * </p>
 * 
 * @since 3.3
 */
public interface IBeanObservable {
	/**
	 * @return property descriptor of the property being observed
	 */
	public PropertyDescriptor getPropertyDescriptor();
	/**
	 * @return instance being observed.  This can be either a bean or an observable (e.g. master detail).
	 */
	public Object getObserved();
}
