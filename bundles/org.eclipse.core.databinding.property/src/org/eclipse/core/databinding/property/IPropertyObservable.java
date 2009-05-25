/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 ******************************************************************************/

package org.eclipse.core.databinding.property;

import org.eclipse.core.databinding.observable.IObserving;

/**
 * Provides access to the details of property observables
 * 
 * @since 1.2
 */
public interface IPropertyObservable extends IObserving {
	/**
	 * Returns the property being observed
	 * 
	 * @return the property being observed
	 */
	IProperty getProperty();
}
