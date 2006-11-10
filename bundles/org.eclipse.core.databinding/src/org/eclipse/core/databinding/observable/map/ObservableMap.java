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

package org.eclipse.core.databinding.observable.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.ListenerList;

/**
 * @since 1.1
 * 
 */
public class ObservableMap extends AbstractObservable implements IObservableMap {

	private ListenerList mapChangeListeners = new ListenerList();
	
	protected Map wrappedMap;

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

	public int size() {
		ObservableTracker.getterCalled(this);
		return wrappedMap.size();
	}

	public Collection values() {
		ObservableTracker.getterCalled(this);
		return wrappedMap.values();
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

	public Object put(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map arg0) {
		throw new UnsupportedOperationException();
	}

}
