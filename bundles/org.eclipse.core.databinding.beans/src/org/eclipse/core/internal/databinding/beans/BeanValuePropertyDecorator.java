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

import org.eclipse.core.databinding.beans.IBeanListProperty;
import org.eclipse.core.databinding.beans.IBeanMapProperty;
import org.eclipse.core.databinding.beans.IBeanSetProperty;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.property.value.ValueProperty;

/**
 * @param <S> type of the source object
 * @param <T> type of the value of the property
 *
 * @since 3.3
 *
 */
public class BeanValuePropertyDecorator<S, T> extends ValueProperty<S, T> implements IBeanValueProperty<S, T> {
	private final IValueProperty<S, T> delegate;
	private final PropertyDescriptor propertyDescriptor;

	/**
	 * @param delegate
	 * @param propertyDescriptor
	 */
	public BeanValuePropertyDecorator(IValueProperty<S, T> delegate, PropertyDescriptor propertyDescriptor) {
		this.delegate = delegate;
		this.propertyDescriptor = propertyDescriptor;
	}

	@Override
	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}

	@Override
	public Object getValueType() {
		return delegate.getValueType();
	}

	@Override
	protected T doGetValue(S source) {
		return delegate.getValue(source);
	}

	@Override
	protected void doSetValue(S source, T value) {
		delegate.setValue(source, value);
	}

	@Override
	public IBeanValueProperty<S, Object> value(String propertyName) {
		return value(propertyName, null);
	}

	@Override
	public <T2> IBeanValueProperty<S, T2> value(String propertyName, Class<T2> valueType) {
		@SuppressWarnings("unchecked")
		Class<T> beanClass = (Class<T>) delegate.getValueType();
		return value(BeanProperties.value(beanClass, propertyName, valueType));
	}

	@Override
	public <T2> IBeanValueProperty<S, T2> value(IBeanValueProperty<? super T, T2> property) {
		return new BeanValuePropertyDecorator<>(super.value(property), property.getPropertyDescriptor());
	}

	@Override
	public IBeanListProperty<S, Object> list(String propertyName) {
		return list(propertyName, null);
	}

	@Override
	public <E> IBeanListProperty<S, E> list(String propertyName, Class<E> elementType) {
		@SuppressWarnings("unchecked")
		Class<T> beanClass = (Class<T>) delegate.getValueType();
		return list(BeanProperties.list(beanClass, propertyName, elementType));
	}

	@Override
	public <E> IBeanListProperty<S, E> list(IBeanListProperty<? super T, E> property) {
		return new BeanListPropertyDecorator<>(super.list(property), property.getPropertyDescriptor());
	}

	@Override
	public IBeanSetProperty<S, Object> set(String propertyName) {
		return set(propertyName, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> IBeanSetProperty<S, E> set(String propertyName, Class<E> elementType) {
		Class<T> beanClass = (Class<T>) delegate.getValueType();
		return set(BeanProperties.set(beanClass, propertyName, elementType));
	}

	@Override
	public <E> IBeanSetProperty<S, E> set(IBeanSetProperty<? super T, E> property) {
		return new BeanSetPropertyDecorator<>(super.set(property), property.getPropertyDescriptor());
	}

	@Override
	public IBeanMapProperty<S, Object, Object> map(String propertyName) {
		return map(propertyName, null, null);
	}

	@Override
	public <K, V> IBeanMapProperty<S, K, V> map(String propertyName, Class<K> keyType, Class<V> valueType) {
		@SuppressWarnings("unchecked")
		Class<T> beanClass = (Class<T>) delegate.getValueType();
		return map(BeanProperties.map(beanClass, propertyName, keyType, valueType));
	}

	@Override
	public <K, V> IBeanMapProperty<S, K, V> map(IBeanMapProperty<? super T, K, V> property) {
		return new BeanMapPropertyDecorator<>(super.map(property), property.getPropertyDescriptor());
	}

	@Override
	public IObservableValue<T> observe(S source) {
		return new BeanObservableValueDecorator<>(delegate.observe(source), propertyDescriptor);
	}

	@Override
	public IObservableValue<T> observe(Realm realm, S source) {
		return new BeanObservableValueDecorator<>(delegate.observe(realm, source), propertyDescriptor);
	}

	@Override
	public <U extends S> IObservableValue<T> observeDetail(IObservableValue<U> master) {
		return new BeanObservableValueDecorator<>(delegate.observeDetail(master), propertyDescriptor);
	}

	@Override
	public <U extends S> IObservableList<T> observeDetail(IObservableList<U> master) {
		return new BeanObservableListDecorator<>(delegate.observeDetail(master), propertyDescriptor);
	}

	@Override
	public <U extends S> IObservableMap<U, T> observeDetail(IObservableSet<U> master) {
		return new BeanObservableMapDecorator<>(delegate.observeDetail(master), propertyDescriptor);
	}

	@Override
	public <K, V extends S> IObservableMap<K, T> observeDetail(IObservableMap<K, V> master) {
		return new BeanObservableMapDecorator<>(delegate.observeDetail(master), propertyDescriptor);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
