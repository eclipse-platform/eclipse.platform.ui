/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 265561, 262287, 268203, 268688, 301774, 303847
 *     Ovidio Mallo - bug 332367
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.map;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.AbstractObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.map.SimpleMapProperty;

/**
 * @param <S>
 *            type of the source object
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 * @since 1.2
 */
public class SimplePropertyObservableMap<S, K, V> extends AbstractObservableMap<K, V>
		implements IPropertyObservable<SimpleMapProperty<S, K, V>> {
	private S source;
	private SimpleMapProperty<S, K, V> property;

	private volatile boolean updating = false;

	private volatile int modCount = 0;

	private INativePropertyListener<S> listener;

	private Map<K, V> cachedMap;
	private boolean stale;

	/**
	 * @param realm
	 * @param source
	 * @param property
	 */
	public SimplePropertyObservableMap(Realm realm, S source, SimpleMapProperty<S, K, V> property) {
		super(realm);
		this.source = source;
		this.property = property;
	}

	@Override
	public Object getKeyType() {
		return property.getKeyType();
	}

	@Override
	public Object getValueType() {
		return property.getValueType();
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	@Override
	protected void firstListenerAdded() {
		if (!isDisposed() && listener == null) {
			listener = property.adaptListener(new ISimplePropertyListener<S, MapDiff<K, V>>() {
				@Override
				public void handleEvent(final SimplePropertyEvent<S, MapDiff<K, V>> event) {
					if (!isDisposed() && !updating) {
						getRealm().exec(new Runnable() {
							@Override
							public void run() {
								if (event.type == SimplePropertyEvent.CHANGE) {
									modCount++;
									notifyIfChanged(event.diff);
								} else if (event.type == SimplePropertyEvent.STALE && !stale) {
									stale = true;
									fireStale();
								}
							}
						});
					}
				}
			});
		}

		getRealm().exec(new Runnable() {
			@Override
			public void run() {
				cachedMap = new HashMap<>(getMap());
				stale = false;

				if (listener != null)
					listener.addTo(source);
			}
		});
	}

	@Override
	protected void lastListenerRemoved() {
		if (listener != null)
			listener.removeFrom(source);

		cachedMap.clear();
		cachedMap = null;
		stale = false;
	}

	// Queries

	private Map<K, V> getMap() {
		return property.getMap(source);
	}

	// Single change operations

	private void updateMap(Map<K, V> map, MapDiff<K, V> diff) {
		if (!diff.isEmpty()) {
			boolean wasUpdating = updating;
			updating = true;
			try {
				property.updateMap(source, diff);
				modCount++;
			} finally {
				updating = wasUpdating;
			}

			notifyIfChanged(null);
		}
	}

	private EntrySet es = new EntrySet();

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		getterCalled();
		return es;
	}

	private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntrySetIterator();
		}

		@Override
		public int size() {
			return getMap().size();
		}
	}

	private class EntrySetIterator implements Iterator<Map.Entry<K, V>> {
		private volatile int expectedModCount = modCount;
		Map<K, V> map = new HashMap<>(getMap());
		Iterator<Map.Entry<K, V>> iterator = map.entrySet().iterator();
		Map.Entry<K, V> last = null;

		@Override
		public boolean hasNext() {
			getterCalled();
			checkForComodification();
			return iterator.hasNext();
		}

		@Override
		public Map.Entry<K, V> next() {
			getterCalled();
			checkForComodification();
			last = iterator.next();
			return last;
		}

		@Override
		public void remove() {
			getterCalled();
			checkForComodification();

			MapDiff<K, V> diff = Diffs.createMapDiffSingleRemove(last.getKey(), last.getValue());
			updateMap(map, diff);

			iterator.remove(); // stay in sync

			last = null;
			expectedModCount = modCount;
		}

		private void checkForComodification() {
			if (expectedModCount != modCount)
				throw new ConcurrentModificationException();
		}
	}

	@Override
	public Set<K> keySet() {
		getterCalled();
		// AbstractMap depends on entrySet() to fulfil keySet() API, so all
		// getterCalled() and comodification checks will still be handled
		return super.keySet();
	}

	@Override
	public boolean containsKey(Object key) {
		getterCalled();

		return getMap().containsKey(key);
	}

	@Override
	public V get(Object key) {
		getterCalled();

		return getMap().get(key);
	}

	@Override
	public V put(K key, V value) {
		checkRealm();

		Map<K, V> map = getMap();

		boolean add = !map.containsKey(key);

		V oldValue = map.get(key);

		MapDiff<K, V> diff;
		if (add)
			diff = Diffs.createMapDiffSingleAdd(key, value);
		else
			diff = Diffs.createMapDiffSingleChange(key, oldValue, value);

		updateMap(map, diff);

		return oldValue;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		checkRealm();

		Map<K, V> map = getMap();

		Map<K, V> oldValues = new HashMap<K, V>();
		Map<K, V> newValues = new HashMap<K, V>();
		Set<K> changedKeys = new HashSet<K>();
		Set<K> addedKeys = new HashSet<K>();
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
			K key = entry.getKey();
			V newValue = entry.getValue();
			if (map.containsKey(key)) {
				changedKeys.add(key);
				oldValues.put(key, map.get(key));
			} else {
				addedKeys.add(key);
			}
			newValues.put(key, newValue);
		}

		MapDiff<K, V> diff = Diffs.createMapDiff(addedKeys, Collections.<K> emptySet(), changedKeys, oldValues,
				newValues);
		updateMap(map, diff);
	}

	@Override
	public V remove(Object key) {
		checkRealm();

		Map<K, V> map = getMap();
		if (!map.containsKey(key))
			return null;

		V oldValue = map.get(key);

		@SuppressWarnings("unchecked")
		// if we contain this key, then it is of
		// type K
		MapDiff<K, V> diff = Diffs.createMapDiffSingleRemove((K) key, oldValue);
		updateMap(map, diff);

		return oldValue;
	}

	@Override
	public void clear() {
		getterCalled();

		Map<K, V> map = getMap();
		if (map.isEmpty())
			return;

		MapDiff<K, V> diff = Diffs.createMapDiffRemoveAll(new HashMap<K, V>(map));
		updateMap(map, diff);
	}

	@Override
	public Collection<V> values() {
		getterCalled();
		// AbstractMap depends on entrySet() to fulfil values() API, so all
		// getterCalled() and comodification checks will still be handled
		return super.values();
	}

	private void notifyIfChanged(MapDiff<K, V> diff) {
		if (hasListeners()) {
			Map<K, V> oldMap = cachedMap;
			Map<K, V> newMap = cachedMap = new HashMap<K, V>(getMap());
			if (diff == null)
				diff = Diffs.computeMapDiff(oldMap, newMap);
			if (!diff.isEmpty() || stale) {
				stale = false;
				fireMapChange(diff);
			}
		}
	}

	@Override
	public boolean isStale() {
		getterCalled();
		return stale;
	}

	@Override
	public Object getObserved() {
		return source;
	}

	@Override
	public SimpleMapProperty<S, K, V> getProperty() {
		return property;
	}

	@Override
	public synchronized void dispose() {
		if (!isDisposed()) {
			if (listener != null)
				listener.removeFrom(source);
			property = null;
			source = null;
			listener = null;
			stale = false;
		}
		super.dispose();
	}
}
