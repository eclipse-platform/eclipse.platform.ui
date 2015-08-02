/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

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
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.map.AbstractObservableMap;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;
import org.eclipse.core.internal.databinding.property.Util;

/**
 * @param <S>
 *            type of the source object
 * @param <I>
 *            type of the intermediate values
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 * @since 1.2
 */
public class MapDelegatingValueObservableMap<S, K, I extends S, V> extends AbstractObservableMap<K, V>
		implements IPropertyObservable<DelegatingValueProperty<S, V>> {
	private IObservableMap<K, I> masterMap;
	private DelegatingValueProperty<S, V> detailProperty;
	private DelegatingCache<S, I, V> cache;

	private Set<Map.Entry<K, V>> entrySet;

	class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new Iterator<Map.Entry<K, V>>() {
				Iterator<Map.Entry<K, I>> it = masterMap.entrySet().iterator();

				@Override
				public boolean hasNext() {
					getterCalled();
					return it.hasNext();
				}

				@Override
				public Map.Entry<K, V> next() {
					getterCalled();
					Map.Entry<K, I> next = it.next();
					return new MapEntry(next.getKey());
				}

				@Override
				public void remove() {
					it.remove();
				}
			};
		}

		@Override
		public int size() {
			return masterMap.size();
		}
	}

	class MapEntry implements Map.Entry<K, V> {
		private K key;

		MapEntry(K key) {
			this.key = key;
		}

		@Override
		public K getKey() {
			getterCalled();
			return key;
		}

		@Override
		public V getValue() {
			getterCalled();

			if (!masterMap.containsKey(key))
				return null;

			I masterValue = masterMap.get(key);
			return cache.get(masterValue);
		}

		@Override
		public V setValue(V value) {
			checkRealm();

			if (!masterMap.containsKey(key))
				return null;

			I masterValue = masterMap.get(key);
			return cache.put(masterValue, value);
		}

		@Override
		public boolean equals(Object o) {
			getterCalled();
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<?, ?> that = (Map.Entry<?, ?>) o;
			return Util.equals(this.getKey(), that.getKey()) && Util.equals(this.getValue(), that.getValue());
		}

		@Override
		public int hashCode() {
			getterCalled();
			Object value = getValue();
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}
	}

	private IMapChangeListener<K, I> masterListener = new IMapChangeListener<K, I>() {
		@Override
		public void handleMapChange(final MapChangeEvent<? extends K, ? extends I> event) {
			if (isDisposed())
				return;

			cache.addAll(masterMap.values());

			// Need both obsolete and new master values to convert diff
			MapDiff<K, V> diff = convertDiff(event.diff);

			cache.retainAll(masterMap.values());

			fireMapChange(diff);
		}

		private MapDiff<K, V> convertDiff(MapDiff<? extends K, ? extends I> diff) {
			Map<K, V> oldValues = new HashMap<>();
			Map<K, V> newValues = new HashMap<>();

			Set<? extends K> addedKeys = diff.getAddedKeys();
			for (K key : addedKeys) {
				I masterValue = diff.getNewValue(key);
				V newValue = cache.get(masterValue);
				newValues.put(key, newValue);
			}

			Set<? extends K> removedKeys = diff.getRemovedKeys();
			for (K key : removedKeys) {
				I masterValue = diff.getOldValue(key);
				V oldValue = cache.get(masterValue);
				oldValues.put(key, oldValue);
			}

			Set<K> changedKeys = new HashSet<K>(diff.getChangedKeys());
			for (Iterator<K> it = changedKeys.iterator(); it.hasNext();) {
				K key = it.next();

				I oldMasterValue = diff.getOldValue(key);
				I newMasterValue = diff.getNewValue(key);

				V oldValue = cache.get(oldMasterValue);
				V newValue = cache.get(newMasterValue);

				if (Util.equals(oldValue, newValue)) {
					it.remove();
				} else {
					oldValues.put(key, oldValue);
					newValues.put(key, newValue);
				}
			}

			return Diffs.createMapDiff(addedKeys, removedKeys, changedKeys, oldValues, newValues);
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		@Override
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	/**
	 * @param map
	 * @param valueProperty
	 */
	public MapDelegatingValueObservableMap(IObservableMap<K, I> map, DelegatingValueProperty<S, V> valueProperty) {
		super(map.getRealm());
		this.masterMap = map;
		this.detailProperty = valueProperty;
		this.cache = new DelegatingCache<S, I, V>(getRealm(), valueProperty) {
			@Override
			void handleValueChange(I masterElement, V oldValue, V newValue) {
				fireMapChange(keysFor(masterElement), oldValue, newValue);
			}
		};
		cache.addAll(masterMap.values());

		masterMap.addMapChangeListener(masterListener);
		masterMap.addStaleListener(staleListener);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		getterCalled();
		if (entrySet == null)
			entrySet = new EntrySet();
		return entrySet;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	@Override
	public V get(Object key) {
		getterCalled();
		Object masterValue = masterMap.get(key);
		return cache.get(masterValue);
	}

	@Override
	public V put(K key, V value) {
		if (!masterMap.containsKey(key))
			return null;
		I masterValue = masterMap.get(key);
		return cache.put(masterValue, value);
	}

	@Override
	public boolean isStale() {
		getterCalled();
		return masterMap.isStale();
	}

	@Override
	public Object getObserved() {
		return masterMap;
	}

	@Override
	public DelegatingValueProperty<S, V> getProperty() {
		return detailProperty;
	}

	@Override
	public Object getKeyType() {
		return masterMap.getKeyType();
	}

	@Override
	public Object getValueType() {
		return detailProperty.getValueType();
	}

	private Set<K> keysFor(I masterValue) {
		Set<K> keys = new HashSet<>();

		for (Map.Entry<K, I> entry : masterMap.entrySet()) {
			if (entry.getValue() == masterValue) {
				keys.add(entry.getKey());
			}
		}

		return keys;
	}

	private void fireMapChange(final Set<K> changedKeys, final V oldValue, final V newValue) {
		fireMapChange(new MapDiff<K, V>() {
			@Override
			public Set<K> getAddedKeys() {
				return Collections.emptySet();
			}

			@Override
			public Set<K> getRemovedKeys() {
				return Collections.emptySet();
			}

			@Override
			public Set<K> getChangedKeys() {
				return Collections.unmodifiableSet(changedKeys);
			}

			@Override
			public V getOldValue(Object key) {
				if (changedKeys.contains(key))
					return oldValue;
				return null;
			}

			@Override
			public V getNewValue(Object key) {
				if (changedKeys.contains(key))
					return newValue;
				return null;
			}
		});
	}

	@Override
	public synchronized void dispose() {
		if (masterMap != null) {
			masterMap.removeMapChangeListener(masterListener);
			masterMap.removeStaleListener(staleListener);
			masterMap = null;
		}

		if (cache != null) {
			cache.dispose();
			cache = null;
		}

		masterListener = null;
		detailProperty = null;

		super.dispose();
	}
}
