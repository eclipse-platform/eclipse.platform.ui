/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *     Brad Reynolds - bug 164653
 *     Matthew Hall - bug 237718, 226289
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservable;

/**
 * Observable Map.
 *
 * @param <K>
 *            type of the keys in the map
 * @param <V>
 *            type of the values in the map
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the classes that
 *              implement this interface. Note that direct implementers of this
 *              interface outside of the framework will be broken in future
 *              releases when methods are added to this interface.
 *
 * @see AbstractObservableMap
 * @see ObservableMap
 *
 * @since 1.1
 */
public interface IObservableMap<K, V> extends Map<K, V>, IObservable {

	/**
	 * Returns the element type for the {@link #keySet() keyset} of this
	 * observable map, or <code>null</code> if the keyset is untyped.
	 *
	 * @return the element type for the {@link #keySet() keyset} of this
	 *         observable map, or <code>null</code> if the keyset is untyped.
	 * @since 1.2
	 */
	Object getKeyType();

	/**
	 * Returns the element type for the {@link #values() values} of this
	 * observable map, or <code>null</code> if the values collection is untyped.
	 *
	 * @return the element type for the {@link #values() values} of this
	 *         observable map, or <code>null</code> if the values collection is
	 *         untyped.
	 * @since 1.2
	 */
	Object getValueType();

	/**
	 * @param listener
	 */
	void addMapChangeListener(IMapChangeListener<? super K, ? super V> listener);

	/**
	 * @param listener
	 */
	void removeMapChangeListener(IMapChangeListener<? super K, ? super V> listener);

	/**
	 * @TrackedGetter
	 */
	@Override int size();

	/**
	 * @TrackedGetter
	 */
	@Override boolean isEmpty();

	/**
	 * @TrackedGetter
	 */
	@Override boolean containsKey(Object key);

	/**
	 * @TrackedGetter
	 */
	@Override boolean containsValue(Object value);

	/**
	 * @TrackedGetter
	 */
	@Override V get(Object key);

	/**
	 *
	 */
	@Override V put(K key, V value);

	/**
	 *
	 */
	@Override V remove(Object key);

	/**
	 * @TrackedGetter
	 */
	@Override Set<K> keySet();

	/**
	 * @TrackedGetter
	 */
	@Override Collection<V> values();

	/**
	 * @TrackedGetter
	 */
	@Override Set<Map.Entry<K, V>> entrySet();

	/**
	 * @TrackedGetter
	 */
	@Override boolean equals(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override int hashCode();
}
