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
import java.util.Map;

import org.eclipse.core.databinding.beans.IBeanMapProperty;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.databinding.property.map.MapProperty;

/**
 * @param <S> type of the source object
 * @param <K> type of the keys to the map
 * @param <V> type of the values in the map
 *
 * @since 3.3
 *
 */
public class BeanMapPropertyDecorator<S, K, V> extends MapProperty<S, K, V> implements IBeanMapProperty<S, K, V> {
	private final IMapProperty<S, K, V> delegate;
	private final PropertyDescriptor propertyDescriptor;

	/**
	 * @param delegate
	 * @param propertyDescriptor
	 */
	public BeanMapPropertyDecorator(IMapProperty<S, K, V> delegate,
			PropertyDescriptor propertyDescriptor) {
		this.delegate = delegate;
		this.propertyDescriptor = propertyDescriptor;
	}

	@Override
	public PropertyDescriptor getPropertyDescriptor() {
		return propertyDescriptor;
	}

	@Override
	public Object getKeyType() {
		return delegate.getKeyType();
	}

	@Override
	public Object getValueType() {
		return delegate.getValueType();
	}

	@Override
	protected Map<K, V> doGetMap(S source) {
		return delegate.getMap(source);
	}

	@Override
	protected void doSetMap(S source, Map<K, V> map) {
		delegate.setMap(source, map);
	}

	@Override
	protected void doUpdateMap(S source, MapDiff<K, V> diff) {
		delegate.updateMap(source, diff);
	}

	@Override
	public <V2> IBeanMapProperty<S, K, V2> values(String propertyName) {
		return values(propertyName, null);
	}

	@Override
	public <V2> IBeanMapProperty<S, K, V2> values(String propertyName, Class<V2> valueType) {
		@SuppressWarnings("unchecked")
		Class<V> beanClass = (Class<V>) delegate.getValueType();
		return values(BeanProperties.value(beanClass, propertyName, valueType));
	}

	@Override
	public <V2> IBeanMapProperty<S, K, V2> values(IBeanValueProperty<? super V, V2> property) {
		return new BeanMapPropertyDecorator<>(super.values(property), property.getPropertyDescriptor());
	}

	@Override
	public IObservableMap<K, V> observe(S source) {
		return new BeanObservableMapDecorator<>(delegate.observe(source), propertyDescriptor);
	}

	@Override
	public IObservableMap<K, V> observe(Realm realm, S source) {
		return new BeanObservableMapDecorator<>(delegate.observe(realm, source), propertyDescriptor);
	}

	@Override
	public <U extends S> IObservableMap<K, V> observeDetail(IObservableValue<U> master) {
		return new BeanObservableMapDecorator<>(delegate.observeDetail(master), propertyDescriptor);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
