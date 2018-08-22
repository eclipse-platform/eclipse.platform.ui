/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property;

import org.eclipse.core.databinding.observable.IObserving;

/**
 * Provides access to the details of property observables
 *
 * @param <P>
 *            specific type of the value property being observed
 * @since 1.2
 */
public interface IPropertyObservable<P extends IProperty> extends IObserving {
	/**
	 * Returns the property being observed
	 *
	 * @return the property being observed
	 */
	P getProperty();
}
