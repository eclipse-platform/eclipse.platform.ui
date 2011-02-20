/*******************************************************************************
 * Copyright (c) 2010 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 305367)
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
 * @since 1.4
 */
public class MapDetailValueObservableMap extends AbstractObservableMap
		implements IObserving {

	private IObservableMap masterMap;

	private IObservableFactory observableValueFactory;

	private Object detailValueType;

	private Set entrySet;

	private IdentityHashMap keyDetailMap = new IdentityHashMap();

	private IdentitySet staleDetailObservables = new IdentitySet();

	private IMapChangeListener masterMapListener = new IMapChangeListener() {
		public void handleMapChange(MapChangeEvent event) {
			handleMasterMapChange(event.diff);
		}
	};

	private IStaleListener masterStaleListener = new IStaleListener() {
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	private IStaleListener detailStaleListener = new IStaleListener() {
		public void handleStale(StaleEvent staleEvent) {
			addStaleDetailObservable((IObservableValue) staleEvent
					.getObservable());
		}
	};

	/**
	 * @param masterMap
	 * @param observableValueFactory
	 * @param detailValueType
	 */
	public MapDetailValueObservableMap(IObservableMap masterMap,
			IObservableFactory observableValueFactory, Object detailValueType) {
		super(masterMap.getRealm());
		this.masterMap = masterMap;
		this.observableValueFactory = observableValueFactory;
		this.detailValueType = detailValueType;

		// Add change/stale/dispose listeners on the master map.
		masterMap.addMapChangeListener(masterMapListener);
		masterMap.addStaleListener(masterStaleListener);
		masterMap.addDisposeListener(new IDisposeListener() {
			public void handleDispose(DisposeEvent event) {
				MapDetailValueObservableMap.this.dispose();
			}
		});

		// Initialize the map with the current state of the master map.
		MapDiff initMasterDiff = Diffs.computeMapDiff(Collections.EMPTY_MAP,
				masterMap);
		handleMasterMapChange(initMasterDiff);
	}

	private void handleMasterMapChange(MapDiff diff) {
		// Collect the detail values for the master values in the input diff.
		IdentityMap oldValues = new IdentityMap();
		IdentityMap newValues = new IdentityMap();

		// Handle added master values.
		Set addedKeys = diff.getAddedKeys();
		for (Iterator iter = addedKeys.iterator(); iter.hasNext();) {
			Object addedKey = iter.next();

			// For added master values, we set up a new detail observable.
			addDetailObservable(addedKey);

			// Get the value of the created detail observable for the new diff.
			IObservableValue detailValue = getDetailObservableValue(addedKey);
			newValues.put(addedKey, detailValue.getValue());
		}

		// Handle removed master values.
		Set removedKeys = diff.getRemovedKeys();
		for (Iterator iter = removedKeys.iterator(); iter.hasNext();) {
			Object removedKey = iter.next();

			// First of all, get the current detail value and add it to the set
			// of old values of the new diff.
			IObservableValue detailValue = getDetailObservableValue(removedKey);
			oldValues.put(removedKey, detailValue.getValue());

			// For removed master values, we dispose the detail observable.
			removeDetailObservable(removedKey);
		}

		// Handle changed master values.
		Set changedKeys = diff.getChangedKeys();
		for (Iterator iter = changedKeys.iterator(); iter.hasNext();) {
			Object changedKey = iter.next();

			// Get the detail value prior to the change and add it to the set of
			// old values of the new diff.
			IObservableValue oldDetailValue = getDetailObservableValue(changedKey);
			oldValues.put(changedKey, oldDetailValue.getValue());

			// Remove the old detail value for the old master value and add it
			// again for the new master value.
			removeDetailObservable(changedKey);
			addDetailObservable(changedKey);

			// Get the new detail value and add it to the set of new values.
			IObservableValue newDetailValue = getDetailObservableValue(changedKey);
			newValues.put(changedKey, newDetailValue.getValue());
		}

		// The different key sets are the same, only the values change.
		fireMapChange(Diffs.createMapDiff(addedKeys, removedKeys, changedKeys,
				oldValues, newValues));
	}

	private void addDetailObservable(final Object addedKey) {
		Object masterElement = masterMap.get(addedKey);

		IObservableValue detailValue = (IObservableValue) keyDetailMap
				.get(addedKey);

		if (detailValue == null) {
			detailValue = createDetailObservable(masterElement);

			keyDetailMap.put(addedKey, detailValue);

			detailValue.addValueChangeListener(new IValueChangeListener() {
				public void handleValueChange(ValueChangeEvent event) {
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

	private IObservableValue createDetailObservable(Object masterElement) {
		ObservableTracker.setIgnore(true);
		try {
			return (IObservableValue) observableValueFactory
					.createObservable(masterElement);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	private void removeDetailObservable(Object removedKey) {
		if (isDisposed()) {
			return;
		}

		IObservableValue detailValue = (IObservableValue) keyDetailMap
				.remove(removedKey);
		staleDetailObservables.remove(detailValue);
		detailValue.dispose();
	}

	private IObservableValue getDetailObservableValue(Object masterKey) {
		return (IObservableValue) keyDetailMap.get(masterKey);
	}

	private void addStaleDetailObservable(IObservableValue detailObservable) {
		boolean wasStale = isStale();
		staleDetailObservables.add(detailObservable);
		if (!wasStale) {
			fireStale();
		}
	}

	public Set keySet() {
		getterCalled();

		return masterMap.keySet();
	}

	public Object get(Object key) {
		getterCalled();

		if (!containsKey(key)) {
			return null;
		}

		IObservableValue detailValue = getDetailObservableValue(key);
		return detailValue.getValue();
	}

	public Object put(Object key, Object value) {
		if (!containsKey(key)) {
			return null;
		}

		IObservableValue detailValue = getDetailObservableValue(key);
		Object oldValue = detailValue.getValue();
		detailValue.setValue(value);
		return oldValue;
	}

	public boolean containsKey(Object key) {
		getterCalled();

		return masterMap.containsKey(key);
	}

	public Object remove(Object key) {
		checkRealm();

		if (!containsKey(key)) {
			return null;
		}

		IObservableValue detailValue = getDetailObservableValue(key);
		Object oldValue = detailValue.getValue();

		masterMap.remove(key);

		return oldValue;
	}

	public int size() {
		getterCalled();

		return masterMap.size();
	}

	public boolean isStale() {
		return super.isStale()
				|| (masterMap != null && masterMap.isStale())
				|| (staleDetailObservables != null && !staleDetailObservables
						.isEmpty());
	}

	public Object getKeyType() {
		return masterMap.getKeyType();
	}

	public Object getValueType() {
		return detailValueType;
	}

	public Object getObserved() {
		return masterMap;
	}

	public synchronized void dispose() {
		if (masterMap != null) {
			masterMap.removeMapChangeListener(masterMapListener);
			masterMap.removeStaleListener(masterStaleListener);
		}

		if (keyDetailMap != null) {
			for (Iterator iter = keyDetailMap.values().iterator(); iter
					.hasNext();) {
				IObservableValue detailValue = (IObservableValue) iter.next();
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

	public Set entrySet() {
		getterCalled();

		if (entrySet == null) {
			entrySet = new EntrySet();
		}
		return entrySet;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	private class EntrySet extends AbstractSet {

		public Iterator iterator() {
			final Iterator keyIterator = keySet().iterator();
			return new Iterator() {

				public boolean hasNext() {
					return keyIterator.hasNext();
				}

				public Object next() {
					Object key = keyIterator.next();
					return new MapEntry(key);
				}

				public void remove() {
					keyIterator.remove();
				}
			};
		}

		public int size() {
			return MapDetailValueObservableMap.this.size();
		}
	}

	private final class MapEntry implements Map.Entry {

		private final Object key;

		private MapEntry(Object key) {
			this.key = key;
		}

		public Object getKey() {
			MapDetailValueObservableMap.this.getterCalled();
			return key;
		}

		public Object getValue() {
			return MapDetailValueObservableMap.this.get(getKey());
		}

		public Object setValue(Object value) {
			return MapDetailValueObservableMap.this.put(getKey(), value);
		}

		public boolean equals(Object o) {
			MapDetailValueObservableMap.this.getterCalled();
			if (o == this)
				return true;
			if (o == null)
				return false;
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry that = (Map.Entry) o;
			return Util.equals(this.getKey(), that.getKey())
					&& Util.equals(this.getValue(), that.getValue());
		}

		public int hashCode() {
			MapDetailValueObservableMap.this.getterCalled();
			Object value = getValue();
			return (getKey() == null ? 0 : getKey().hashCode())
					^ (value == null ? 0 : value.hashCode());
		}
	}
}
