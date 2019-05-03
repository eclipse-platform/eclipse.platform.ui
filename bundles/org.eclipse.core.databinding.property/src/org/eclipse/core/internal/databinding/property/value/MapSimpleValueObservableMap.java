/*******************************************************************************
 * Copyright (c) 2008, 2017 Matthew Hall and others.
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
 *     Matthew Hall - bugs 262269, 265561, 262287, 268688, 278550, 303847
 *     Ovidio Mallo - bugs 299619, 301370
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.map.AbstractObservableMap;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentityMap;
import org.eclipse.core.internal.databinding.identity.IdentityObservableSet;
import org.eclipse.core.internal.databinding.identity.IdentitySet;

/**
 * @param <S>
 *            type of the source object
 * @param <K>
 *            type of the keys to the map
 * @param <I>
 *            type of the intermediate values
 * @param <V>
 *            type of the values in the map
 * @since 1.2
 *
 */
public class MapSimpleValueObservableMap<S, K, I extends S, V> extends AbstractObservableMap<K, V>
		implements IPropertyObservable<SimpleValueProperty<S, V>> {
	private IObservableMap<K, I> masterMap;
	private SimpleValueProperty<S, V> detailProperty;

	private IObservableSet<I> knownMasterValues;
	private Map<I, V> cachedValues;
	private Set<I> staleMasterValues;

	private boolean updating = false;

	private IMapChangeListener<K, I> masterListener = new IMapChangeListener<K, I>() {
		@Override
		public void handleMapChange(final MapChangeEvent<? extends K, ? extends I> event) {
			if (!isDisposed()) {
				updateKnownValues();
				if (!updating)
					fireMapChange(convertDiff(event.diff));
			}
		}

		private void updateKnownValues() {
			Set<I> knownValues = new IdentitySet<>(masterMap.values());
			knownMasterValues.retainAll(knownValues);
			knownMasterValues.addAll(knownValues);
		}

		private MapDiff<K, V> convertDiff(MapDiff<? extends K, ? extends I> diff) {
			Map<K, V> oldValues = new IdentityMap<>();
			Map<K, V> newValues = new IdentityMap<>();

			Set<? extends K> addedKeys = diff.getAddedKeys();
			for (K key : addedKeys) {
				I newSource = diff.getNewValue(key);
				V newValue = detailProperty.getValue(newSource);
				newValues.put(key, newValue);
			}

			Set<? extends K> removedKeys = diff.getRemovedKeys();
			for (K key : removedKeys) {
				I oldSource = diff.getOldValue(key);
				V oldValue = detailProperty.getValue(oldSource);
				oldValues.put(key, oldValue);
			}

			Set<K> changedKeys = new IdentitySet<K>(diff.getChangedKeys());
			for (Iterator<K> it = changedKeys.iterator(); it.hasNext();) {
				K key = it.next();

				I oldSource = diff.getOldValue(key);
				I newSource = diff.getNewValue(key);

				V oldValue = detailProperty.getValue(oldSource);
				V newValue = detailProperty.getValue(newSource);

				if (Objects.equals(oldValue, newValue)) {
					it.remove();
				} else {
					oldValues.put(key, oldValue);
					newValues.put(key, newValue);
				}
			}

			return Diffs.createMapDiff(addedKeys, removedKeys, changedKeys, oldValues, newValues);
		}
	};

	private IStaleListener staleListener = staleEvent -> fireStale();

	private INativePropertyListener<S> detailListener;

	/**
	 * @param map
	 * @param valueProperty
	 */
	public MapSimpleValueObservableMap(IObservableMap<K, I> map, SimpleValueProperty<S, V> valueProperty) {
		super(map.getRealm());
		this.masterMap = map;
		this.detailProperty = valueProperty;

		ISimplePropertyListener<S, ValueDiff<? extends V>> listener = event -> {
			if (!isDisposed() && !updating) {
				getRealm().exec(() -> {
					@SuppressWarnings("unchecked")
					I source = (I) event.getSource();
					if (event.type == SimplePropertyEvent.CHANGE) {
						notifyIfChanged(source);
					} else if (event.type == SimplePropertyEvent.STALE) {
						boolean wasStale = !staleMasterValues.isEmpty();
						staleMasterValues.add(source);
						if (!wasStale)
							fireStale();
					}
				});
			}
		};
		this.detailListener = detailProperty.adaptListener(listener);
	}

	@Override
	public Object getKeyType() {
		return masterMap.getKeyType();
	}

	@Override
	public Object getValueType() {
		return detailProperty.getValueType();
	}

	@Override
	protected void firstListenerAdded() {
		ObservableTracker.setIgnore(true);
		try {
			knownMasterValues = new IdentityObservableSet<>(getRealm(), null);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		cachedValues = new IdentityMap<>();
		staleMasterValues = new IdentitySet<>();
		knownMasterValues.addSetChangeListener(event -> {
			for (I key1 : event.diff.getRemovals()) {
				if (detailListener != null)
					detailListener.removeFrom(key1);
				cachedValues.remove(key1);
				staleMasterValues.remove(key1);
			}
			for (I key2 : event.diff.getAdditions()) {
				cachedValues.put(key2, detailProperty.getValue(key2));
				if (detailListener != null)
					detailListener.addTo(key2);
			}
		});

		getRealm().exec(() -> {
			knownMasterValues.addAll(masterMap.values());

			masterMap.addMapChangeListener(masterListener);
			masterMap.addStaleListener(staleListener);
		});
	}

	@Override
	protected void lastListenerRemoved() {
		masterMap.removeMapChangeListener(masterListener);
		masterMap.removeStaleListener(staleListener);
		if (knownMasterValues != null) {
			knownMasterValues.dispose();
			knownMasterValues = null;
		}
		cachedValues.clear();
		cachedValues = null;
		staleMasterValues.clear();
		staleMasterValues = null;
	}

	private Set<Map.Entry<K, V>> entrySet;

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		getterCalled();
		if (entrySet == null)
			entrySet = new EntrySet();
		return entrySet;
	}

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
			return detailProperty.getValue(masterMap.get(key));
		}

		@Override
		public V setValue(V value) {
			if (!masterMap.containsKey(key))
				return null;
			I source = masterMap.get(key);

			V oldValue = detailProperty.getValue(source);

			updating = true;
			try {
				detailProperty.setValue(source, value);
			} finally {
				updating = false;
			}

			notifyIfChanged(source);

			return oldValue;
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
			return Objects.equals(this.getKey(), that.getKey()) && Objects.equals(this.getValue(), that.getValue());
		}

		@Override
		public int hashCode() {
			getterCalled();
			return Objects.hashCode(key) ^ Objects.hashCode(getValue());
		}
	}

	@Override
	public boolean containsKey(Object key) {
		getterCalled();

		return masterMap.containsKey(key);
	}

	@Override
	public V get(Object key) {
		getterCalled();

		return detailProperty.getValue(masterMap.get(key));
	}

	@Override
	public V put(K key, V value) {
		if (!masterMap.containsKey(key))
			return null;
		I masterValue = masterMap.get(key);
		V oldValue = detailProperty.getValue(masterValue);
		detailProperty.setValue(masterValue, value);
		notifyIfChanged(masterValue);
		return oldValue;
	}

	@Override
	public V remove(Object key) {
		checkRealm();

		I masterValue = masterMap.get(key);
		V oldValue = detailProperty.getValue(masterValue);

		masterMap.remove(key);

		return oldValue;
	}

	private void notifyIfChanged(I masterValue) {
		if (cachedValues != null) {
			final Set<K> keys = keysFor(masterValue);

			final V oldValue = cachedValues.get(masterValue);
			final V newValue = detailProperty.getValue(masterValue);

			if (!Objects.equals(oldValue, newValue) || staleMasterValues.contains(masterValue)) {
				cachedValues.put(masterValue, newValue);
				staleMasterValues.remove(masterValue);
				fireMapChange(new MapDiff<K, V>() {
					@Override
					public Set<K> getAddedKeys() {
						return Collections.emptySet();
					}

					@Override
					public Set<K> getChangedKeys() {
						return keys;
					}

					@Override
					public Set<K> getRemovedKeys() {
						return Collections.emptySet();
					}

					@Override
					public V getNewValue(Object key) {
						return newValue;
					}

					@Override
					public V getOldValue(Object key) {
						return oldValue;
					}
				});
			}
		}
	}

	private Set<K> keysFor(I value) {
		Set<K> keys = new IdentitySet<K>();

		for (Map.Entry<K, I> entry : masterMap.entrySet()) {
			if (entry.getValue() == value) {
				keys.add(entry.getKey());
			}
		}

		return keys;
	}

	@Override
	public boolean isStale() {
		getterCalled();
		return masterMap.isStale() || staleMasterValues != null && !staleMasterValues.isEmpty();
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	@Override
	public Object getObserved() {
		return masterMap;
	}

	@Override
	public SimpleValueProperty<S, V> getProperty() {
		return detailProperty;
	}

	@Override
	public synchronized void dispose() {
		if (masterMap != null) {
			masterMap.removeMapChangeListener(masterListener);
			masterMap = null;
		}
		if (knownMasterValues != null) {
			knownMasterValues.clear(); // detaches listeners
			knownMasterValues.dispose();
			knownMasterValues = null;
		}

		masterListener = null;
		detailListener = null;
		detailProperty = null;
		cachedValues = null;
		staleMasterValues = null;

		super.dispose();
	}
}
