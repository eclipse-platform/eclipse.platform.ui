/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
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
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;
import org.eclipse.core.internal.databinding.property.Util;

/**
 * @since 1.2
 */
public class MapDelegatingValueObservableMap extends AbstractObservableMap
		implements IPropertyObservable {
	private IObservableMap masterMap;
	private DelegatingValueProperty detailProperty;
	private DelegatingCache cache;

	private Set entrySet;

	class EntrySet extends AbstractSet {
		@Override
		public Iterator iterator() {
			return new Iterator() {
				Iterator it = masterMap.entrySet().iterator();

				@Override
				public boolean hasNext() {
					getterCalled();
					return it.hasNext();
				}

				@Override
				public Object next() {
					getterCalled();
					Map.Entry next = (Map.Entry) it.next();
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

	class MapEntry implements Map.Entry {
		private Object key;

		MapEntry(Object key) {
			this.key = key;
		}

		@Override
		public Object getKey() {
			getterCalled();
			return key;
		}

		@Override
		public Object getValue() {
			getterCalled();

			if (!masterMap.containsKey(key))
				return null;

			Object masterValue = masterMap.get(key);
			return cache.get(masterValue);
		}

		@Override
		public Object setValue(Object value) {
			checkRealm();

			if (!masterMap.containsKey(key))
				return null;

			Object masterValue = masterMap.get(key);
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
			Map.Entry that = (Map.Entry) o;
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

	private IMapChangeListener masterListener = new IMapChangeListener() {
		@Override
		public void handleMapChange(final MapChangeEvent event) {
			if (isDisposed())
				return;

			cache.addAll(masterMap.values());

			// Need both obsolete and new master values to convert diff
			MapDiff diff = convertDiff(event.diff);

			cache.retainAll(masterMap.values());

			fireMapChange(diff);
		}

		private MapDiff convertDiff(MapDiff diff) {
			Map oldValues = new HashMap();
			Map newValues = new HashMap();

			Set addedKeys = diff.getAddedKeys();
			for (Iterator it = addedKeys.iterator(); it.hasNext();) {
				Object key = it.next();
				Object masterValue = diff.getNewValue(key);
				Object newValue = cache.get(masterValue);
				newValues.put(key, newValue);
			}

			Set removedKeys = diff.getRemovedKeys();
			for (Iterator it = removedKeys.iterator(); it.hasNext();) {
				Object key = it.next();
				Object masterValue = diff.getOldValue(key);
				Object oldValue = cache.get(masterValue);
				oldValues.put(key, oldValue);
			}

			Set changedKeys = new HashSet(diff.getChangedKeys());
			for (Iterator it = changedKeys.iterator(); it.hasNext();) {
				Object key = it.next();

				Object oldMasterValue = diff.getOldValue(key);
				Object newMasterValue = diff.getNewValue(key);

				Object oldValue = cache.get(oldMasterValue);
				Object newValue = cache.get(newMasterValue);

				if (Util.equals(oldValue, newValue)) {
					it.remove();
				} else {
					oldValues.put(key, oldValue);
					newValues.put(key, newValue);
				}
			}

			return Diffs.createMapDiff(addedKeys, removedKeys, changedKeys,
					oldValues, newValues);
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
	public MapDelegatingValueObservableMap(IObservableMap map,
			DelegatingValueProperty valueProperty) {
		super(map.getRealm());
		this.masterMap = map;
		this.detailProperty = valueProperty;
		this.cache = new DelegatingCache(getRealm(), valueProperty) {
			@Override
			void handleValueChange(Object masterElement, Object oldValue,
					Object newValue) {
				fireMapChange(keysFor(masterElement), oldValue, newValue);
			}
		};
		cache.addAll(masterMap.values());

		masterMap.addMapChangeListener(masterListener);
		masterMap.addStaleListener(staleListener);
	}

	@Override
	public Set entrySet() {
		getterCalled();
		if (entrySet == null)
			entrySet = new EntrySet();
		return entrySet;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	@Override
	public Object get(Object key) {
		getterCalled();
		Object masterValue = masterMap.get(key);
		return cache.get(masterValue);
	}

	@Override
	public Object put(Object key, Object value) {
		if (!masterMap.containsKey(key))
			return null;
		Object masterValue = masterMap.get(key);
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
	public IProperty getProperty() {
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

	private Set keysFor(Object masterValue) {
		Set keys = new HashSet();

		for (Iterator it = masterMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Entry) it.next();
			if (entry.getValue() == masterValue) {
				keys.add(entry.getKey());
			}
		}

		return keys;
	}

	private void fireMapChange(final Set changedKeys, final Object oldValue,
			final Object newValue) {
		fireMapChange(new MapDiff() {
			@Override
			public Set getAddedKeys() {
				return Collections.EMPTY_SET;
			}

			@Override
			public Set getRemovedKeys() {
				return Collections.EMPTY_SET;
			}

			@Override
			public Set getChangedKeys() {
				return Collections.unmodifiableSet(changedKeys);
			}

			@Override
			public Object getOldValue(Object key) {
				if (changedKeys.contains(key))
					return oldValue;
				return null;
			}

			@Override
			public Object getNewValue(Object key) {
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
