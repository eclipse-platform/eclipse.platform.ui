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
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;
import org.eclipse.core.internal.databinding.property.Util;

/**
 * @since 1.2
 */
public class SetDelegatingValueObservableMap extends AbstractObservableMap
		implements IPropertyObservable {
	private IObservableSet masterSet;
	private DelegatingValueProperty detailProperty;
	private DelegatingCache cache;

	private Set entrySet;

	class EntrySet extends AbstractSet {
		public Iterator iterator() {
			return new Iterator() {
				final Iterator it = masterSet.iterator();

				public boolean hasNext() {
					return it.hasNext();
				}

				public Object next() {
					return new MapEntry(it.next());
				}

				public void remove() {
					it.remove();
				}
			};
		}

		public int size() {
			return masterSet.size();
		}
	}

	class MapEntry implements Map.Entry {
		private final Object key;

		MapEntry(Object key) {
			this.key = key;
		}

		public Object getKey() {
			getterCalled();
			return key;
		}

		public Object getValue() {
			getterCalled();

			if (!masterSet.contains(key))
				return null;

			return cache.get(key);
		}

		public Object setValue(Object value) {
			checkRealm();

			if (!masterSet.contains(key))
				return null;

			return cache.put(key, value);
		}

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

		public int hashCode() {
			getterCalled();
			Object value = getValue();
			return (key == null ? 0 : key.hashCode())
					^ (value == null ? 0 : value.hashCode());
		}
	}

	private ISetChangeListener masterListener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			if (isDisposed())
				return;

			cache.addAll(masterSet);

			// Need both obsolete and new elements to convert diff
			MapDiff diff = convertDiff(event.diff);

			cache.retainAll(masterSet);

			fireMapChange(diff);
		}

		private MapDiff convertDiff(SetDiff diff) {
			// Convert diff to detail value
			Map oldValues = new HashMap();
			Map newValues = new HashMap();

			for (Iterator it = diff.getRemovals().iterator(); it.hasNext();) {
				Object masterElement = it.next();
				oldValues.put(masterElement, cache.get(masterElement));
			}
			for (Iterator it = diff.getAdditions().iterator(); it.hasNext();) {
				Object masterElement = it.next();
				newValues.put(masterElement, cache.get(masterElement));
			}
			return Diffs.createMapDiff(diff.getAdditions(), diff.getRemovals(),
					Collections.EMPTY_SET, oldValues, newValues);
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	/**
	 * @param keySet
	 * @param valueProperty
	 */
	public SetDelegatingValueObservableMap(IObservableSet keySet,
			DelegatingValueProperty valueProperty) {
		super(keySet.getRealm());
		this.masterSet = keySet;
		this.detailProperty = valueProperty;
		this.cache = new DelegatingCache(getRealm(), valueProperty) {
			void handleValueChange(Object masterElement, Object oldValue,
					Object newValue) {
				fireMapChange(Diffs.createMapDiffSingleChange(masterElement,
						oldValue, newValue));
			}
		};
		cache.addAll(masterSet);

		masterSet.addSetChangeListener(masterListener);
		masterSet.addStaleListener(staleListener);
	}

	public Set entrySet() {
		getterCalled();
		if (entrySet == null)
			entrySet = new EntrySet();
		return entrySet;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	public Object get(Object key) {
		getterCalled();
		return cache.get(key);
	}

	public Object put(Object key, Object value) {
		checkRealm();
		return cache.put(key, value);
	}

	public boolean isStale() {
		return masterSet.isStale();
	}

	public Object getObserved() {
		return masterSet;
	}

	public IProperty getProperty() {
		return detailProperty;
	}

	public Object getKeyType() {
		return masterSet.getElementType();
	}

	public Object getValueType() {
		return detailProperty.getValueType();
	}

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
