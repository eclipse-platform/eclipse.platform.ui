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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.map.AbstractObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;
import org.eclipse.core.internal.databinding.property.Util;

/**
 * @param <S>
 *            type of the source object
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 * @since 1.2
 */
public class SetDelegatingValueObservableMap<S, K extends S, V> extends AbstractObservableMap<K, V>
		implements IPropertyObservable<DelegatingValueProperty<S, V>> {
	private IObservableSet<K> masterSet;
	private DelegatingValueProperty<S, V> detailProperty;
	private DelegatingCache<S, K, V> cache;

	private Set<Map.Entry<K, V>> entrySet;

	class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new Iterator<Map.Entry<K, V>>() {
				final Iterator<K> it = masterSet.iterator();

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public Map.Entry<K, V> next() {
					return new MapEntry(it.next());
				}

				@Override
				public void remove() {
					it.remove();
				}
			};
		}

		@Override
		public int size() {
			return masterSet.size();
		}
	}

	class MapEntry implements Map.Entry<K, V> {
		private final K key;

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

			if (!masterSet.contains(key))
				return null;

			return cache.get(key);
		}

		@Override
		public V setValue(V value) {
			checkRealm();

			if (!masterSet.contains(key))
				return null;

			return cache.put(key, value);
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
			return Util.equals(this.getKey(), that.getKey())
					&& Util.equals(this.getValue(), that.getValue());
		}

		@Override
		public int hashCode() {
			getterCalled();
			Object value = getValue();
			return (key == null ? 0 : key.hashCode())
					^ (value == null ? 0 : value.hashCode());
		}
	}

	private ISetChangeListener<K> masterListener = new ISetChangeListener<K>() {
		@Override
		public void handleSetChange(SetChangeEvent<? extends K> event) {
			if (isDisposed())
				return;

			cache.addAll(masterSet);

			// Need both obsolete and new elements to convert diff
			MapDiff<K, V> diff = convertDiff(event.diff);

			cache.retainAll(masterSet);

			fireMapChange(diff);
		}

		private MapDiff<K, V> convertDiff(SetDiff<? extends K> diff) {
			// Convert diff to detail value
			Map<K, V> oldValues = new HashMap<>();
			Map<K, V> newValues = new HashMap<>();

			for (K masterElement : diff.getRemovals()) {
				oldValues.put(masterElement, cache.get(masterElement));
			}
			for (K masterElement : diff.getAdditions()) {
				newValues.put(masterElement, cache.get(masterElement));
			}
			return Diffs.createMapDiff(diff.getAdditions(), diff.getRemovals(),
					Collections.<K> emptySet(), oldValues, newValues);
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		@Override
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	/**
	 * @param keySet
	 * @param valueProperty
	 */
	public SetDelegatingValueObservableMap(IObservableSet<K> keySet,
			DelegatingValueProperty<S, V> valueProperty) {
		super(keySet.getRealm());
		this.masterSet = keySet;
		this.detailProperty = valueProperty;
		this.cache = new DelegatingCache<S, K, V>(getRealm(), valueProperty) {
			@Override
			void handleValueChange(K masterElement, V oldValue, V newValue) {
				fireMapChange(Diffs.createMapDiffSingleChange(masterElement,
						oldValue, newValue));
			}
		};
		cache.addAll(masterSet);

		masterSet.addSetChangeListener(masterListener);
		masterSet.addStaleListener(staleListener);
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
		return cache.get(key);
	}

	@Override
	public V put(K key, V value) {
		checkRealm();
		return cache.put(key, value);
	}

	@Override
	public boolean isStale() {
		return masterSet.isStale();
	}

	@Override
	public Object getObserved() {
		return masterSet;
	}

	@Override
	public DelegatingValueProperty<S, V> getProperty() {
		return detailProperty;
	}

	@Override
	public Object getKeyType() {
		return masterSet.getElementType();
	}

	@Override
	public Object getValueType() {
		return detailProperty.getValueType();
	}

	@Override
	public synchronized void dispose() {
		if (masterSet != null) {
			masterSet.removeSetChangeListener(masterListener);
			masterSet.removeStaleListener(staleListener);
			masterSet = null;
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
