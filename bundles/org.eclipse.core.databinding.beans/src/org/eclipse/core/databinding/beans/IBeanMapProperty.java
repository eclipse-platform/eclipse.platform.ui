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
 ******************************************************************************/

package org.eclipse.core.databinding.beans;

import java.util.Map;

import org.eclipse.core.databinding.property.map.IMapProperty;

/**
 * An {@link IMapProperty} extension interface with convenience methods for
 * creating nested bean properties.
 *
 * @param <S> type of the source object
 * @param <K> type of the keys to the map
 * @param <V> type of the values in the map
 *
 * @since 1.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IBeanMapProperty<S, K, V> extends IBeanProperty, IMapProperty<S, K, V> {
	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property.
	 *
	 * @param propertyName
	 *            the value property to observe. May be nested e.g.
	 *            "parent.name"
	 * @return a master-detail combination of this property and the specified
	 *         value property.
	 * @see #values(IBeanValueProperty)
	 */
	public <V2> IBeanMapProperty<S, K, V2> values(String propertyName);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property.
	 *
	 * @param propertyName
	 *            the value property to observe. May be nested e.g.
	 *            "parent.name"
	 * @param valueType
	 *            the value type of the named property
	 * @return a master-detail combination of this property and the specified
	 *         value property.
	 * @see #values(IBeanValueProperty)
	 */
	public <V2> IBeanMapProperty<S, K, V2> values(String propertyName, Class<V2> valueType);

	/**
	 * Returns a master-detail combination of this property and the specified
	 * value property. The returned property will observe the specified value
	 * property for all {@link Map#values() values} observed by this map
	 * property, mapping from this map property's {@link Map#keySet() key set}
	 * to the specified value property's value for each element in the master
	 * property's {@link Map#values() values} collection.
	 *
	 * @param property
	 *            the detail property to observe
	 * @return a master-detail combination of this property and the specified
	 *         value property.
	 */
	public <V2> IBeanMapProperty<S, K, V2> values(IBeanValueProperty<? super V, V2> property);
}
