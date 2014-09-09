/*******************************************************************************
 * Copyright (c) 2008, 2011 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 265561, 262287, 268203, 268688, 301774, 303847
 *     Ovidio Mallo - bug 332367
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
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.map.SimpleMapProperty;

/**
 * @since 1.2
 */
public class SimplePropertyObservableMap extends AbstractObservableMap
		implements IPropertyObservable {
	private Object source;
	private SimpleMapProperty property;

	private volatile boolean updating = false;

	private volatile int modCount = 0;

	private INativePropertyListener listener;

	private Map cachedMap;
	private boolean stale;

	/**
	 * @param realm
	 * @param source
	 * @param property
	 */
	public SimplePropertyObservableMap(Realm realm, Object source,
			SimpleMapProperty property) {
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
		if (!isDisposed()) {
			if (listener == null) {
				listener = property
						.adaptListener(new ISimplePropertyListener() {
							@Override
							public void handleEvent(
									final SimplePropertyEvent event) {
								if (!isDisposed() && !updating) {
									getRealm().exec(new Runnable() {
										@Override
										public void run() {
											if (event.type == SimplePropertyEvent.CHANGE) {
												modCount++;
												notifyIfChanged((MapDiff) event.diff);
											} else if (event.type == SimplePropertyEvent.STALE
													&& !stale) {
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
					cachedMap = new HashMap(getMap());
					stale = false;

					if (listener != null)
						listener.addTo(source);
				}
			});
		}
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

	private Map getMap() {
		return property.getMap(source);
	}

	// Single change operations

	private void updateMap(Map map, MapDiff diff) {
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
	public Set entrySet() {
		getterCalled();
		return es;
	}

	private class EntrySet extends AbstractSet {
		@Override
		public Iterator iterator() {
			return new EntrySetIterator();
		}

		@Override
		public int size() {
			return getMap().size();
		}
	}

	private class EntrySetIterator implements Iterator {
		private volatile int expectedModCount = modCount;
		Map map = new HashMap(getMap());
		Iterator iterator = map.entrySet().iterator();
		Map.Entry last = null;

		@Override
		public boolean hasNext() {
			getterCalled();
			checkForComodification();
			return iterator.hasNext();
		}

		@Override
		public Object next() {
			getterCalled();
			checkForComodification();
			last = (Map.Entry) iterator.next();
			return last;
		}

		@Override
		public void remove() {
			getterCalled();
			checkForComodification();

			MapDiff diff = Diffs.createMapDiffSingleRemove(last.getKey(),
					last.getValue());
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
	public Set keySet() {
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
	public Object get(Object key) {
		getterCalled();

		return getMap().get(key);
	}

	@Override
	public Object put(Object key, Object value) {
		checkRealm();

		Map map = getMap();

		boolean add = !map.containsKey(key);

		Object oldValue = map.get(key);

		MapDiff diff;
		if (add)
			diff = Diffs.createMapDiffSingleAdd(key, value);
		else
			diff = Diffs.createMapDiffSingleChange(key, oldValue, value);

		updateMap(map, diff);

		return oldValue;
	}

	@Override
	public void putAll(Map m) {
		checkRealm();

		Map map = getMap();

		Map oldValues = new HashMap();
		Map newValues = new HashMap();
		Set changedKeys = new HashSet();
		Set addedKeys = new HashSet();
		for (Iterator it = m.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			Object newValue = entry.getValue();
			if (map.containsKey(key)) {
				changedKeys.add(key);
				oldValues.put(key, map.get(key));
			} else {
				addedKeys.add(key);
			}
			newValues.put(key, newValue);
		}

		MapDiff diff = Diffs.createMapDiff(addedKeys, Collections.EMPTY_SET,
				changedKeys, oldValues, newValues);
		updateMap(map, diff);
	}

	@Override
	public Object remove(Object key) {
		checkRealm();

		Map map = getMap();
		if (!map.containsKey(key))
			return null;

		Object oldValue = map.get(key);

		MapDiff diff = Diffs.createMapDiffSingleRemove(key, oldValue);
		updateMap(map, diff);

		return oldValue;
	}

	@Override
	public void clear() {
		getterCalled();

		Map map = getMap();
		if (map.isEmpty())
			return;

		MapDiff diff = Diffs.createMapDiffRemoveAll(new HashMap(map));
		updateMap(map, diff);
	}

	@Override
	public Collection values() {
		getterCalled();
		// AbstractMap depends on entrySet() to fulfil values() API, so all
		// getterCalled() and comodification checks will still be handled
		return super.values();
	}

	private void notifyIfChanged(MapDiff diff) {
		if (hasListeners()) {
			Map oldMap = cachedMap;
			Map newMap = cachedMap = new HashMap(getMap());
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
	public IProperty getProperty() {
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
