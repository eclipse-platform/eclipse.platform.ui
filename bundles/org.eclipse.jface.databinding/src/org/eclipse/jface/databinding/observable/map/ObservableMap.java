/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.observable.map;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.databinding.observable.AbstractObservable;
import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.databinding.observable.ObservableTracker;
import org.eclipse.jface.databinding.observable.Realm;

/**
 * @since 1.1
 * 
 */
public class ObservableMap extends AbstractObservable implements IObservableMap {

	private ListenerList mapChangeListeners = new ListenerList();
	
	private Map wrappedMap;

	private boolean stale = false;
	
	/**
	 * @param realm 
	 * @param wrappedMap
	 */
	public ObservableMap(Map wrappedMap) {
		this(Realm.getDefault(), wrappedMap);
	}

	/**
	 * @param realm 
	 * @param wrappedMap
	 */
	public ObservableMap(Realm realm, Map wrappedMap) {
		super(realm);
		this.wrappedMap = wrappedMap;
	}
	
	public void addMapChangeListener(IMapChangeListener listener) {
		mapChangeListeners.add(listener);
	}

	public void removeMapChangeListener(IMapChangeListener listener) {
		mapChangeListeners.remove(listener);
	}

	protected boolean hasListeners() {
		return super.hasListeners() || !mapChangeListeners.isEmpty();
	}

	protected void fireMapChange(MapDiff diff) {
		// fire general change event first
		super.fireChange();

		Object[] listeners = mapChangeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IMapChangeListener) listeners[i]).handleMapChange(this, diff);
		}
	}

	public boolean containsKey(Object key) {
		ObservableTracker.getterCalled(this);
		return wrappedMap.containsKey(key);
	}

	public boolean containsValue(Object value) {
		ObservableTracker.getterCalled(this);
		return wrappedMap.containsValue(value);
	}

	public Set entrySet() {
		ObservableTracker.getterCalled(this);
		return wrappedMap.entrySet();
	}

	public Object get(Object key) {
		ObservableTracker.getterCalled(this);
		return wrappedMap.get(key);
	}

	public boolean isEmpty() {
		ObservableTracker.getterCalled(this);
		return wrappedMap.isEmpty();
	}

	public Set keySet() {
		ObservableTracker.getterCalled(this);
		return wrappedMap.keySet();
	}

	public Object put(Object key, Object value) {
		ObservableTracker.getterCalled(this);
		Object result = wrappedMap.put(key, value);
		if (result==null) {
			fireMapChange(Diffs.createMapDiffSingleAdd(key, value));
		} else {
			fireMapChange(Diffs.createMapDiffSingleChange(key, value, result));
		}
		return result;
	}

	public Object remove(Object key) {
		ObservableTracker.getterCalled(this);
		Object result = wrappedMap.remove(key);
		if (result!=null) {
			fireMapChange(Diffs.createMapDiffSingleRemove(key, result));
		}
		return result;
	}

	public int size() {
		ObservableTracker.getterCalled(this);
		return wrappedMap.size();
	}

	public Collection values() {
		ObservableTracker.getterCalled(this);
		return wrappedMap.values();
	}

	public void clear() {
		Map copy = new HashMap(wrappedMap.size());
		copy.putAll(wrappedMap);
		wrappedMap.clear();
		fireMapChange(Diffs.createMapDiffRemoveAll(copy));
	}

	public void putAll(Map map) {
		Set addedKeys = new HashSet(map.size());
		Map changes = new HashMap(map.size());
		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Entry) it.next();
			Object previousValue = wrappedMap.put(entry.getKey(), entry.getValue());
			if (previousValue==null) {
				addedKeys.add(entry.getKey());
			} else {
				changes.put(entry.getKey(), previousValue);
			}
		}
		fireMapChange(Diffs.createMapDiff(addedKeys, Collections.EMPTY_SET, changes.keySet(), changes, wrappedMap));
	}

	/**
	 * @return Returns the stale state.
	 */
	public boolean isStale() {
		return stale;
	}

	/**
	 * @param stale
	 *            The stale state to set. This will fire a stale event if the
	 *            given boolean is true and this observable set was not already
	 *            stale.
	 */
	public void setStale(boolean stale) {
		boolean wasStale = this.stale;
		this.stale = stale;
		if (!wasStale && stale) {
			fireStale();
		}
	}

}
