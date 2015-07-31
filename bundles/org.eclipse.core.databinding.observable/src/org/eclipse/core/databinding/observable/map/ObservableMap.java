/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Matthew Hall - bugs 226289, 274450
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *     Stefan Xenos <sxenos@gmail.com> - Bug 474065
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;

/**
 *
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 *
 * @param <K>
 *            the type of the keys in this map
 * @param <V>
 *            the type of the values in this map
 *
 * @since 1.0
 */
public class ObservableMap<K, V> extends AbstractObservable implements IObservableMap<K, V> {

	protected Map<K, V> wrappedMap;

	private boolean stale = false;

	/**
	 * @param wrappedMap
	 */
	public ObservableMap(Map<K, V> wrappedMap) {
		this(Realm.getDefault(), wrappedMap);
	}

	/**
	 * @param realm
	 * @param wrappedMap
	 */
	public ObservableMap(Realm realm, Map<K, V> wrappedMap) {
		super(realm);
		this.wrappedMap = wrappedMap;
	}

	@Override
	public synchronized void addMapChangeListener(IMapChangeListener<? super K, ? super V> listener) {
		addListener(MapChangeEvent.TYPE, listener);
	}

	@Override
	public synchronized void removeMapChangeListener(IMapChangeListener<? super K, ? super V> listener) {
		removeListener(MapChangeEvent.TYPE, listener);
	}

	/**
	 * @since 1.2
	 */
	@Override
	public Object getKeyType() {
		return null;
	}

	/**
	 * @since 1.2
	 */
	@Override
	public Object getValueType() {
		return null;
	}

	protected void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	protected void fireMapChange(MapDiff<K, V> diff) {
		checkRealm();

		// fire general change event first
		super.fireChange();

		fireEvent(new MapChangeEvent<>(this, diff));
	}

	@Override
	public boolean containsKey(Object key) {
		getterCalled();
		return wrappedMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		getterCalled();
		return wrappedMap.containsValue(value);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		getterCalled();
		return wrappedMap.entrySet();
	}

	@Override
	public V get(Object key) {
		getterCalled();
		return wrappedMap.get(key);
	}

	@Override
	public boolean isEmpty() {
		getterCalled();
		return wrappedMap.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		getterCalled();
		return wrappedMap.keySet();
	}

	@Override
	public int size() {
		getterCalled();
		return wrappedMap.size();
	}

	@Override
	public Collection<V> values() {
		getterCalled();
		return wrappedMap.values();
	}

	/**
	 * Returns the stale state. Must be invoked from the current realm.
	 *
	 * @return stale state
	 */
	@Override
	public boolean isStale() {
		checkRealm();
		return stale;
	}

	/**
	 * Sets the stale state. Must be invoked from the current realm.
	 *
	 * @param stale
	 *            The stale state to set. This will fire a stale event if the
	 *            given boolean is true and this observable set was not already
	 *            stale.
	 */
	public void setStale(boolean stale) {
		checkRealm();
		boolean wasStale = this.stale;
		this.stale = stale;
		if (!wasStale && stale) {
			fireStale();
		}
	}

	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object o) {
		getterCalled();
		return o == this || wrappedMap.equals(o);
	}

	@Override
	public int hashCode() {
		getterCalled();
		return wrappedMap.hashCode();
	}

	@Override
	public synchronized void dispose() {
		super.dispose();
	}
}
