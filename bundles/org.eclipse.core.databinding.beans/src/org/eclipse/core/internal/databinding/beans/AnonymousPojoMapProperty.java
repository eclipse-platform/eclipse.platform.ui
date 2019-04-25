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
 *     Matthew Hall - initial API and implementation (bug 247997)
 *     Matthew Hall - bug 264307
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.property.map.DelegatingMapProperty;
import org.eclipse.core.databinding.property.map.IMapProperty;

/**
 * @param <S> type of the source object
 * @param <K> type of the keys to the map
 * @param <V> type of the values in the map
 *
 * @since 3.3
 *
 */
public class AnonymousPojoMapProperty<S, K, V> extends DelegatingMapProperty<S, K, V> {
	private final String propertyName;

	private Map<Class<S>, IMapProperty<S, K, V>> delegates;

	/**
	 * @param propertyName
	 * @param keyType
	 * @param valueType
	 */
	public AnonymousPojoMapProperty(String propertyName, Class<K> keyType, Class<V> valueType) {
		super(keyType, valueType);
		this.propertyName = propertyName;
		this.delegates = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected IMapProperty<S, K, V> doGetDelegate(S source) {
		Class<S> beanClass = (Class<S>) source.getClass();
		if (delegates.containsKey(beanClass))
			return delegates.get(beanClass);

		IMapProperty<S, K, V> delegate;
		try {
			delegate = PojoProperties.map(beanClass, propertyName,
					(Class<K>) getKeyType(), (Class<V>) getValueType());
		} catch (IllegalArgumentException noSuchProperty) {
			delegate = null;
		}
		delegates.put(beanClass, delegate);
		return delegate;
	}

	@Override
	public String toString() {
		String s = "?." + propertyName + "{:}"; //$NON-NLS-1$ //$NON-NLS-2$
		Class<?> keyType = (Class<?>) getKeyType();
		Class<?> valueType = (Class<?>) getValueType();
		if (keyType != null || valueType != null) {
			s += "<" + BeanPropertyHelper.shortClassName(keyType) + ", " //$NON-NLS-1$//$NON-NLS-2$
					+ BeanPropertyHelper.shortClassName(valueType) + ">"; //$NON-NLS-1$
		}
		return s;
	}
}
