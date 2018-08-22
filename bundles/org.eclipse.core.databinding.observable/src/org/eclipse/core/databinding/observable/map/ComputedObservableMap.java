/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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
 *     Matthew Hall - bugs 241585, 247394, 226289, 194734, 190881, 266754,
 *                    268688
 *     Ovidio Mallo - bug 303847
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.internal.databinding.identity.IdentitySet;

/**
 * Maps objects to one of their attributes. Tracks changes to the underlying
 * observable set of objects (keys), as well as changes to attribute values.
 *
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 */
public abstract class ComputedObservableMap<K, V> extends AbstractObservableMap<K, V> {

	private IObservableSet<K> keySet;

	private Set<K> knownKeys;

	private Object valueType;

	private ISetChangeListener<K> setChangeListener = event -> {
		Set<K> addedKeys = new HashSet<K>(event.diff.getAdditions());
		Set<K> removedKeys = new HashSet<K>(event.diff.getRemovals());
		Map<K, V> oldValues = new HashMap<>();
		Map<K, V> newValues = new HashMap<>();
		for (K removedKey : removedKeys) {
			V oldValue = null;
			if (removedKey != null) {
				oldValue = doGet(removedKey);
				unhookListener(removedKey);
				knownKeys.remove(removedKey);
			}
			oldValues.put(removedKey, oldValue);
		}
		for (K addedKey : addedKeys) {
			V newValue = null;
			if (addedKey != null) {
				newValue = doGet(addedKey);
				hookListener(addedKey);
				knownKeys.add(addedKey);
			}
			newValues.put(addedKey, newValue);
		}
		Set<K> changedKeys = Collections.emptySet();
		fireMapChange(Diffs.createMapDiff(addedKeys, removedKeys, changedKeys, oldValues, newValues));
	};

	private IStaleListener staleListener = staleEvent -> fireStale();

	private Set<Map.Entry<K, V>> entrySet = new EntrySet();

	private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			final Iterator<K> keyIterator = keySet.iterator();
			return new Iterator<Map.Entry<K, V>>() {

				@Override
				public boolean hasNext() {
					return keyIterator.hasNext();
				}

				@Override
				public Map.Entry<K, V> next() {
					final K key = keyIterator.next();
					return new Map.Entry<K, V>() {

						@Override
						public K getKey() {
							getterCalled();
							return key;
						}

						@Override
						public V getValue() {
							return get(getKey());
						}

						@Override
						public V setValue(V value) {
							return put(getKey(), value);
						}
					};
				}

				@Override
				public void remove() {
					keyIterator.remove();
				}
			};
		}

		@Override
		public int size() {
			return keySet.size();
		}

	}

	/**
	 * @param keySet
	 */
	public ComputedObservableMap(IObservableSet<K> keySet) {
		this(keySet, null);
	}

	/**
	 * @param keySet
	 * @param valueType
	 * @since 1.2
	 */
	public ComputedObservableMap(IObservableSet<K> keySet, Object valueType) {
		super(keySet.getRealm());
		this.keySet = keySet;
		this.valueType = valueType;

		keySet.addDisposeListener(disposeEvent -> ComputedObservableMap.this.dispose());
	}

	/**
	 * @deprecated Subclasses are no longer required to call this method.
	 */
	@Deprecated
	protected void init() {
	}

	@Override
	protected void firstListenerAdded() {
		getRealm().exec(() -> hookListeners());
	}

	@Override
	protected void lastListenerRemoved() {
		unhookListeners();
	}

	private void hookListeners() {
		if (keySet != null) {
			knownKeys = new IdentitySet<>();
			keySet.addSetChangeListener(setChangeListener);
			keySet.addStaleListener(staleListener);
			for (K key : this.keySet) {
				hookListener(key);
				knownKeys.add(key);
			}
		}
	}

	private void unhookListeners() {
		if (keySet != null) {
			keySet.removeSetChangeListener(setChangeListener);
			keySet.removeStaleListener(staleListener);
		}
		if (knownKeys != null) {
			Set<K> immutableKnownKeys = Collections.unmodifiableSet(knownKeys);
			for (K key : immutableKnownKeys) {
				unhookListener(key);
			}
			knownKeys.clear();
			knownKeys = null;
		}
	}

	protected final void fireSingleChange(K key, V oldValue, V newValue) {
		fireMapChange(Diffs.createMapDiffSingleChange(key, oldValue, newValue));
	}

	/**
	 * @since 1.2
	 */
	@Override
	public Object getKeyType() {
		return keySet.getElementType();
	}

	/**
	 * @since 1.2
	 */
	@Override
	public Object getValueType() {
		return valueType;
	}

	/**
	 * @since 1.3
	 */
	@Override
	public V remove(Object key) {
		checkRealm();

		V oldValue = get(key);
		keySet().remove(key);

		return oldValue;
	}

	/**
	 * @since 1.3
	 */
	@Override
	public boolean containsKey(Object key) {
		getterCalled();
		return keySet().contains(key);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return entrySet;
	}

	@Override
	public Set<K> keySet() {
		return keySet;
	}

	@SuppressWarnings("unchecked")
	@Override
	final public V get(Object key) {
		getterCalled();
		if (!keySet.contains(key))
			return null;
		return doGet((K) key);
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	@Override
	final public V put(K key, V value) {
		checkRealm();
		if (!keySet.contains(key))
			return null;
		return doPut(key, value);
	}

	/**
	 * @param removedKey
	 */
	protected abstract void unhookListener(K removedKey);

	/**
	 * @param addedKey
	 */
	protected abstract void hookListener(K addedKey);

	/**
	 * @param key
	 * @return the value for the given key
	 */
	protected abstract V doGet(K key);

	/**
	 * @param key
	 * @param value
	 * @return the old value for the given key
	 */
	protected abstract V doPut(K key, V value);

	@Override
	public boolean isStale() {
		return super.isStale() || keySet.isStale();
	}

	@Override
	public synchronized void dispose() {
		unhookListeners();
		entrySet = null;
		keySet = null;
		setChangeListener = null;
		super.dispose();
	}
}
