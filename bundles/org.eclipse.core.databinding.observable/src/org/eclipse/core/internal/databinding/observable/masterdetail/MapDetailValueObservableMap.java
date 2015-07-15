/*******************************************************************************
 * Copyright (c) 2010, 2015 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 305367)
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable.masterdetail;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.map.AbstractObservableMap;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.internal.databinding.identity.IdentityMap;
import org.eclipse.core.internal.databinding.identity.IdentitySet;
import org.eclipse.core.internal.databinding.observable.Util;

/**
 * @param <K>
 *            type of the keys (the keys to both the given master observable map
 *            and the keys to the returned detail map, both of which are the
 *            same set of keys)
 * @param <M>
 *            type of the master observables in the master set, being the values
 *            of the given master observable map
 * @param <E>
 *            type of the detail elements, being the values of the returned
 *            detail map
 * @since 1.4
 */
public class MapDetailValueObservableMap<K, M, E> extends
		AbstractObservableMap<K, E> implements IObserving {

	private IObservableMap<K, M> masterMap;

	private IObservableFactory<? super M, IObservableValue<E>> observableValueFactory;

	private Object detailValueType;

	private Set<Map.Entry<K, E>> entrySet;

	private IdentityHashMap<K, IObservableValue<E>> keyDetailMap = new IdentityHashMap<>();

	private IdentitySet<IObservableValue<E>> staleDetailObservables = new IdentitySet<>();

	private IMapChangeListener<K, M> masterMapListener = new IMapChangeListener<K, M>() {
		@Override
		public void handleMapChange(MapChangeEvent<? extends K, ? extends M> event) {
			handleMasterMapChange(event.diff);
		}
	};

	private IStaleListener masterStaleListener = new IStaleListener() {
		@Override
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	private IStaleListener detailStaleListener = new IStaleListener() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleStale(StaleEvent staleEvent) {
			addStaleDetailObservable((IObservableValue<E>) staleEvent.getObservable());
		}
	};

	/**
	 * @param masterMap
	 * @param observableValueFactory
	 * @param detailValueType
	 */
	public MapDetailValueObservableMap(
			IObservableMap<K, M> masterMap,
			IObservableFactory<? super M, IObservableValue<E>> observableValueFactory,
			Object detailValueType) {
		super(masterMap.getRealm());
		this.masterMap = masterMap;
		this.observableValueFactory = observableValueFactory;
		this.detailValueType = detailValueType;

		// Add change/stale/dispose listeners on the master map.
		masterMap.addMapChangeListener(masterMapListener);
		masterMap.addStaleListener(masterStaleListener);
		masterMap.addDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent event) {
				MapDetailValueObservableMap.this.dispose();
			}
		});

		// Initialize the map with the current state of the master map.
		Map<K, M> emptyMap = Collections.emptyMap();
		MapDiff<K, M> initMasterDiff = Diffs.computeMapDiff(emptyMap, masterMap);
		handleMasterMapChange(initMasterDiff);
	}

	private void handleMasterMapChange(MapDiff<? extends K, ? extends M> diff) {
		// Collect the detail values for the master values in the input diff.
		IdentityMap<K, E> oldValues = new IdentityMap<>();
		IdentityMap<K, E> newValues = new IdentityMap<>();

		// Handle added master values.
		Set<? extends K> addedKeys = diff.getAddedKeys();
		for (K addedKey : addedKeys) {
			// For added master values, we set up a new detail observable.
			addDetailObservable(addedKey);

			// Get the value of the created detail observable for the new diff.
			IObservableValue<E> detailValue = getDetailObservableValue(addedKey);
			newValues.put(addedKey, detailValue.getValue());
		}

		// Handle removed master values.
		Set<? extends K> removedKeys = diff.getRemovedKeys();
		for (K removedKey : removedKeys) {
			// First of all, get the current detail value and add it to the set
			// of old values of the new diff.
			IObservableValue<E> detailValue = getDetailObservableValue(removedKey);
			oldValues.put(removedKey, detailValue.getValue());

			// For removed master values, we dispose the detail observable.
			removeDetailObservable(removedKey);
		}

		// Handle changed master values.
		Set<? extends K> changedKeys = diff.getChangedKeys();
		for (K changedKey : changedKeys) {
			// Get the detail value prior to the change and add it to the set of
			// old values of the new diff.
			IObservableValue<E> oldDetailValue = getDetailObservableValue(changedKey);
			oldValues.put(changedKey, oldDetailValue.getValue());

			// Remove the old detail value for the old master value and add it
			// again for the new master value.
			removeDetailObservable(changedKey);
			addDetailObservable(changedKey);

			// Get the new detail value and add it to the set of new values.
			IObservableValue<E> newDetailValue = getDetailObservableValue(changedKey);
			newValues.put(changedKey, newDetailValue.getValue());
		}

		// The different key sets are the same, only the values change.
		fireMapChange(Diffs.createMapDiff(addedKeys, removedKeys, changedKeys,
				oldValues, newValues));
	}

	private void addDetailObservable(final K addedKey) {
		M masterElement = masterMap.get(addedKey);

		IObservableValue<E> detailValue = keyDetailMap.get(addedKey);

		if (detailValue == null) {
			detailValue = createDetailObservable(masterElement);

			keyDetailMap.put(addedKey, detailValue);

			detailValue.addValueChangeListener(new IValueChangeListener<E>() {
				@Override
				public void handleValueChange(ValueChangeEvent<? extends E> event) {
					if (!event.getObservableValue().isStale()) {
						staleDetailObservables.remove(event.getSource());
					}

					fireMapChange(Diffs.createMapDiffSingleChange(addedKey,
							event.diff.getOldValue(), event.diff.getNewValue()));
				}
			});

			if (detailValue.isStale()) {
				addStaleDetailObservable(detailValue);
			}
		}

		detailValue.addStaleListener(detailStaleListener);
	}

	private IObservableValue<E> createDetailObservable(M masterElement) {
		ObservableTracker.setIgnore(true);
		try {
			return observableValueFactory.createObservable(masterElement);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	private void removeDetailObservable(Object removedKey) {
		if (isDisposed()) {
			return;
		}

		IObservableValue<E> detailValue = keyDetailMap.remove(removedKey);
		staleDetailObservables.remove(detailValue);
		detailValue.dispose();
	}

	private IObservableValue<E> getDetailObservableValue(Object masterKey) {
		return keyDetailMap.get(masterKey);
	}

	private void addStaleDetailObservable(IObservableValue<E> detailObservable) {
		boolean wasStale = isStale();
		staleDetailObservables.add(detailObservable);
		if (!wasStale) {
			fireStale();
		}
	}

	@Override
	public Set<K> keySet() {
		getterCalled();

		return masterMap.keySet();
	}

	@Override
	public E get(Object key) {
		getterCalled();

		if (!containsKey(key)) {
			return null;
		}

		IObservableValue<E> detailValue = getDetailObservableValue(key);
		return detailValue.getValue();
	}

	@Override
	public E put(K key, E value) {
		if (!containsKey(key)) {
			return null;
		}

		IObservableValue<E> detailValue = getDetailObservableValue(key);
		E oldValue = detailValue.getValue();
		detailValue.setValue(value);
		return oldValue;
	}

	@Override
	public boolean containsKey(Object key) {
		getterCalled();

		return masterMap.containsKey(key);
	}

	@Override
	public E remove(Object key) {
		checkRealm();

		if (!containsKey(key)) {
			return null;
		}

		IObservableValue<E> detailValue = getDetailObservableValue(key);
		E oldValue = detailValue.getValue();

		masterMap.remove(key);

		return oldValue;
	}

	@Override
	public int size() {
		getterCalled();

		return masterMap.size();
	}

	@Override
	public boolean isStale() {
		return super.isStale()
				|| (masterMap != null && masterMap.isStale())
				|| (staleDetailObservables != null && !staleDetailObservables
						.isEmpty());
	}

	@Override
	public Object getKeyType() {
		return masterMap.getKeyType();
	}

	@Override
	public Object getValueType() {
		return detailValueType;
	}

	@Override
	public Object getObserved() {
		return masterMap;
	}

	@Override
	public synchronized void dispose() {
		if (masterMap != null) {
			masterMap.removeMapChangeListener(masterMapListener);
			masterMap.removeStaleListener(masterStaleListener);
		}

		if (keyDetailMap != null) {
			for (IObservableValue<E> detailValue : keyDetailMap.values()) {
				detailValue.dispose();
			}
			keyDetailMap.clear();
		}

		masterMap = null;
		observableValueFactory = null;
		detailValueType = null;
		keyDetailMap = null;
		masterStaleListener = null;
		detailStaleListener = null;
		staleDetailObservables = null;

		super.dispose();
	}

	@Override
	public Set<Map.Entry<K, E>> entrySet() {
		getterCalled();

		if (entrySet == null) {
			entrySet = new EntrySet();
		}
		return entrySet;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	private class EntrySet extends AbstractSet<Map.Entry<K, E>> {

		@Override
		public Iterator<Map.Entry<K, E>> iterator() {
			final Iterator<K> keyIterator = keySet().iterator();
			return new Iterator<Map.Entry<K, E>>() {

				@Override
				public boolean hasNext() {
					return keyIterator.hasNext();
				}

				@Override
				public Map.Entry<K, E> next() {
					K key = keyIterator.next();
					return new MapEntry(key);
				}

				@Override
				public void remove() {
					keyIterator.remove();
				}
			};
		}

		@Override
		public int size() {
			return MapDetailValueObservableMap.this.size();
		}
	}

	private final class MapEntry implements Map.Entry<K, E> {

		private final K key;

		private MapEntry(K key) {
			this.key = key;
		}

		@Override
		public K getKey() {
			MapDetailValueObservableMap.this.getterCalled();
			return key;
		}

		@Override
		public E getValue() {
			return MapDetailValueObservableMap.this.get(getKey());
		}

		@Override
		public E setValue(E value) {
			return MapDetailValueObservableMap.this.put(getKey(), value);
		}

		@Override
		public boolean equals(Object o) {
			MapDetailValueObservableMap.this.getterCalled();
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
			MapDetailValueObservableMap.this.getterCalled();
			Object value = getValue();
			return (getKey() == null ? 0 : getKey().hashCode())
					^ (value == null ? 0 : value.hashCode());
		}
	}
}
