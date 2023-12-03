/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/
package org.eclipse.core.databinding.observable.value;

/**
 * An observable value whose changes can be vetoed by listeners.
 *
 * @param <T>
 *            the type of value being observed
 *
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the classes that
 *              implement this interface. Note that direct implementers of this
 *              interface outside of the framework will be broken in future
 *              releases when methods are added to this interface.
 *
 * @since 1.0
 */
public interface IVetoableValue<T> extends IObservableValue<T> {

	/**
	 * @param listener the listener to add; not <code>null</code>
	 */
	public void addValueChangingListener(IValueChangingListener<T> listener);

	/**
	 * @param listener the listener to remove; not <code>null</code>
	 */
	public void removeValueChangingListener(IValueChangingListener<T> listener);

}
