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
 *     Matthew Hall - bugs 195222, 264307, 265561, 301774
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.map.SimpleMapProperty;

/**
 * @param <S> type of the source object
 * @param <K> type of the keys to the map
 * @param <V> type of the values in the map
 *
 * @since 3.3
 */
public class BeanMapProperty<S, K, V> extends SimpleMapProperty<S, K, V> {
	private final PropertyDescriptor propertyDescriptor;
	private final Class<K> keyType;
	private final Class<V> valueType;

	public BeanMapProperty(PropertyDescriptor propertyDescriptor, Class<K> keyType, Class<V> valueType) {
		this.propertyDescriptor = propertyDescriptor;
		this.keyType = keyType;
		this.valueType = valueType;
	}

	@Override
	public Object getKeyType() {
		return keyType;
	}

	@Override
	public Object getValueType() {
		return valueType;
	}

	@Override
	protected Map<K, V> doGetMap(S source) {
		return asMap(BeanPropertyHelper.readProperty(source, propertyDescriptor));
	}

	@SuppressWarnings("unchecked")
	private Map<K, V> asMap(Object propertyValue) {
		if (propertyValue == null)
			return Collections.emptyMap();
		return (Map<K, V>) propertyValue;
	}

	@Override
	protected void doSetMap(S source, Map<K, V> map, MapDiff<K, V> diff) {
		doSetMap(source, map);
	}

	@Override
	protected void doSetMap(S source, Map<K, V> map) {
		BeanPropertyHelper.writeProperty(source, propertyDescriptor, map);
	}

	@Override
	public INativePropertyListener<S> adaptListener(final ISimplePropertyListener<S, MapDiff<K, V>> listener) {
		return new BeanPropertyListener<S, Map<K, V>, MapDiff<K, V>>(this, propertyDescriptor, listener) {
			@Override
			protected MapDiff<K, V> computeDiff(Map<K, V> oldValue, Map<K, V> newValue) {
				return Diffs.computeMapDiff(asMap(oldValue), asMap(newValue));
			}
		};
	}

	@Override
	public String toString() {
		String s = BeanPropertyHelper.propertyName(propertyDescriptor) + "{:}"; //$NON-NLS-1$

		if (keyType != null || valueType != null)
			s += "<" + BeanPropertyHelper.shortClassName(keyType) + ", " //$NON-NLS-1$ //$NON-NLS-2$
					+ BeanPropertyHelper.shortClassName(valueType) + ">"; //$NON-NLS-1$
		return s;
	}
}
