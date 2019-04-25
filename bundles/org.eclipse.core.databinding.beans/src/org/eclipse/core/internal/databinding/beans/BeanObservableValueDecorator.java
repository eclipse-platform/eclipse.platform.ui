/*******************************************************************************
 * Copyright (c) 2007, 2015 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 246625
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.beans.IBeanObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.value.DecoratingObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * {@link IBeanObservable} decorator for an {@link IObservableValue}.
 *
 * @param <T> the type of value being observed
 *
 * @since 3.3
 */
public class BeanObservableValueDecorator<T> extends DecoratingObservableValue<T> implements IBeanObservable {
	private PropertyDescriptor propertyDescriptor;

	/**
	 * @param decorated
	 * @param propertyDescriptor
	 */
	public BeanObservableValueDecorator(IObservableValue<T> decorated, PropertyDescriptor propertyDescriptor) {
		super(decorated, true);
		this.propertyDescriptor = propertyDescriptor;
	}

	@Override
	public synchronized void dispose() {
		this.propertyDescriptor = null;
		super.dispose();
	}

	@Override
	public Object getObserved() {
		IObservable decorated = getDecorated();
		if (decorated instanceof IObserving)
			return ((IObserving) decorated).getObserved();
		return null;
	}

	@Override
	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}
}
