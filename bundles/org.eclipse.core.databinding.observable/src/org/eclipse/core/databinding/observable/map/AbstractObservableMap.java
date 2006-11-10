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

import java.util.AbstractMap;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.ListenerList;

/**
 * @since 3.3
 * 
 */
public abstract class AbstractObservableMap extends AbstractMap implements
		IObservableMap {

	private Realm realm;

	private ListenerList mapListeners = new ListenerList(ListenerList.IDENTITY);

	private ListenerList changeListeners = new ListenerList(
			ListenerList.IDENTITY);

	private ListenerList staleListeners = new ListenerList(
			ListenerList.IDENTITY);

	private boolean stale;

	/**
	 */
	public AbstractObservableMap() {
		this(Realm.getDefault());
	}

	/**
	 * @param realm
	 */
	public AbstractObservableMap(Realm realm) {
		this.realm = realm;
	}

	public void addMapChangeListener(IMapChangeListener listener) {
		mapListeners.add(listener);
	}

	public void removeMapChangeListener(IMapChangeListener listener) {
		mapListeners.remove(listener);
	}

	public void addChangeListener(IChangeListener listener) {
		changeListeners.add(listener);
	}

	public void addStaleListener(IStaleListener listener) {
		staleListeners.add(listener);
	}

	public void dispose() {
		mapListeners = null;
		changeListeners = null;
		staleListeners = null;
	}

	public Realm getRealm() {
		return realm;
	}

	public boolean isStale() {
		return stale;
	}

	public void removeChangeListener(IChangeListener listener) {
		changeListeners.remove(listener);
	}

	public void removeStaleListener(IStaleListener listener) {
		staleListeners.remove(listener);
	}

	/**
	 * @param stale
	 */
	public void setStale(boolean stale) {
		this.stale = stale;
		if (stale) {
			fireStale();
		}
	}

	/**
	 * 
	 */
	protected void fireStale() {
		Object[] listeners = staleListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IStaleListener listener = (IStaleListener) listeners[i];
			listener.handleStale(this);
		}
	}

	protected void fireChange() {
		Object[] listeners = changeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IChangeListener listener = (IChangeListener) listeners[i];
			listener.handleChange(this);
		}
	}

	protected void fireMapChange(MapDiff diff) {
		Object[] listeners = mapListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IMapChangeListener listener = (IMapChangeListener) listeners[i];
			listener.handleMapChange(this, diff);
		}
	}

}
