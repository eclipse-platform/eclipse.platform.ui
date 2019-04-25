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
 *     Matthew Hall - initial API and implementation (bug 195222)
 *     Matthew Hall - bug 264307
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.util.Set;

import org.eclipse.core.databinding.beans.IBeanMapProperty;
import org.eclipse.core.databinding.beans.IBeanSetProperty;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.set.SetProperty;

/**
 * @param <S> type of the source object
 * @param <E> type of the elements in the set
 *
 * @since 3.3
 *
 */
public class BeanSetPropertyDecorator<S, E> extends SetProperty<S, E> implements IBeanSetProperty<S, E> {
	private final ISetProperty<S, E> delegate;
	private final PropertyDescriptor propertyDescriptor;

	/**
	 * @param delegate
	 * @param propertyDescriptor
	 */
	public BeanSetPropertyDecorator(ISetProperty<S, E> delegate,
			PropertyDescriptor propertyDescriptor) {
		this.delegate = delegate;
		this.propertyDescriptor = propertyDescriptor;
	}

	@Override
	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}

	@Override
	public Object getElementType() {
		return delegate.getElementType();
	}

	@Override
	protected Set<E> doGetSet(S source) {
		return delegate.getSet(source);
	}

	@Override
	protected void doSetSet(S source, Set<E> set) {
		delegate.setSet(source, set);
	}

	@Override
	protected void doUpdateSet(S source, SetDiff<E> diff) {
		delegate.updateSet(source, diff);
	}

	@Override
	public IBeanMapProperty<S, E, Object> values(String propertyName) {
		return values(propertyName, null);
	}

	@Override
	public <V> IBeanMapProperty<S, E, V> values(String propertyName, Class<V> valueType) {
		@SuppressWarnings("unchecked")
		Class<E> beanClass = (Class<E>) delegate.getElementType();
		return values(BeanProperties.value(beanClass, propertyName, valueType));
	}

	@Override
	public <V> IBeanMapProperty<S, E, V> values(IBeanValueProperty<? super E, V> property) {
		return new BeanMapPropertyDecorator<>(super.values(property), property.getPropertyDescriptor());
	}

	@Override
	public IObservableSet<E> observe(S source) {
		return new BeanObservableSetDecorator<>(delegate.observe(source), propertyDescriptor);
	}

	@Override
	public IObservableSet<E> observe(Realm realm, S source) {
		return new BeanObservableSetDecorator<>(delegate.observe(realm, source), propertyDescriptor);
	}

	@Override
	public <U extends S> IObservableSet<E> observeDetail(IObservableValue<U> master) {
		return new BeanObservableSetDecorator<>(delegate.observeDetail(master), propertyDescriptor);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
