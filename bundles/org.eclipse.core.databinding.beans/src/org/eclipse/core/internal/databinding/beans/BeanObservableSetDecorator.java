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
import org.eclipse.core.databinding.observable.set.DecoratingObservableSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;

/**
 * {@link IBeanObservable} decorator for an {@link IObservableSet}.
 *
 * @param <E> the type of the elements in this set
 *
 * @since 3.3
 */
public class BeanObservableSetDecorator<E> extends DecoratingObservableSet<E> implements IBeanObservable {
	private PropertyDescriptor propertyDescriptor;

	/**
	 * @param decorated
	 * @param propertyDescriptor
	 */
	public BeanObservableSetDecorator(IObservableSet<E> decorated, PropertyDescriptor propertyDescriptor) {
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
