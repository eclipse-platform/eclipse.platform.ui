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
import java.util.List;

import org.eclipse.core.databinding.beans.IBeanListProperty;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.list.ListProperty;

/**
 * @param <S> type of the source object
 * @param <E> type of the elements in the list
 *
 * @since 3.3
 *
 */
public class PojoListPropertyDecorator<S, E> extends ListProperty<S, E> implements IBeanListProperty<S, E> {
	private final IListProperty<S, E> delegate;
	private final PropertyDescriptor propertyDescriptor;

	/**
	 * @param delegate
	 * @param propertyDescriptor
	 */
	public PojoListPropertyDecorator(IListProperty<S, E> delegate, PropertyDescriptor propertyDescriptor) {
		this.delegate = delegate;
		this.propertyDescriptor = propertyDescriptor;
	}

	@Override
	public Object getElementType() {
		return delegate.getElementType();
	}

	@Override
	protected List<E> doGetList(S source) {
		return delegate.getList(source);
	}

	@Override
	protected void doSetList(S source, List<E> list) {
		delegate.setList(source, list);
	}

	@Override
	protected void doUpdateList(S source, ListDiff<E> diff) {
		delegate.updateList(source, diff);
	}

	@Override
	public IBeanListProperty<S, Object> values(String propertyName) {
		return values(propertyName, null);
	}

	@Override
	public <E2> IBeanListProperty<S, E2> values(String propertyName, Class<E2> valueType) {
		@SuppressWarnings("unchecked")
		Class<E> beanClass = (Class<E>) delegate.getElementType();
		IBeanValueProperty<E, E2> p = PojoProperties.value(beanClass, propertyName, valueType);
		return values(p);
	}

	@Override
	public <T> IBeanListProperty<S, T> values(IBeanValueProperty<? super E, T> property) {
		return new PojoListPropertyDecorator<>(super.values(property), property.getPropertyDescriptor());
	}

	@Override
	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}

	@Override
	public IObservableList<E> observe(S source) {
		return new BeanObservableListDecorator<>(delegate.observe(source), propertyDescriptor);
	}

	@Override
	public IObservableList<E> observe(Realm realm, S source) {
		return new BeanObservableListDecorator<>(delegate.observe(realm, source), propertyDescriptor);
	}

	@Override
	public <U extends S> IObservableList<E> observeDetail(IObservableValue<U> master) {
		return new BeanObservableListDecorator<>(delegate.observeDetail(master), propertyDescriptor);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
