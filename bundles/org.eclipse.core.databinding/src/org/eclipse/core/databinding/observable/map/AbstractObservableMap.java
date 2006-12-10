/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 ******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.AbstractMap;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.ListenerList;

/**
 * @since 3.3
 * 
 */
public abstract class AbstractObservableMap extends AbstractMap implements
		IObservableMap {

	private Realm realm;

	/**
	 * List of {@link IMapChangeListener IMapChangeListeners}.  Access must be synchronized.
	 */
	private ListenerList mapListeners = new ListenerList(ListenerList.IDENTITY);

	/**
	 * List of {@link IChangeListener IChangeListeners}.  Access must by synchronized.
	 */
	private ListenerList changeListeners = new ListenerList(
			ListenerList.IDENTITY);

	/**
	 * List of {@link IStaleListener IStaleListeners}.  Access must be synchronized.
	 */
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

	public synchronized void addMapChangeListener(IMapChangeListener listener) {
		mapListeners.add(listener);
	}

	public synchronized void removeMapChangeListener(IMapChangeListener listener) {
		mapListeners.remove(listener);
	}

	public synchronized void addChangeListener(IChangeListener listener) {
		changeListeners.add(listener);
	}

	public synchronized void addStaleListener(IStaleListener listener) {
		staleListeners.add(listener);
	}

	public synchronized void dispose() {
		mapListeners = null;
		changeListeners = null;
		staleListeners = null;
	}

	public Realm getRealm() {
		return realm;
	}

	public boolean isStale() {
		checkRealm();
		return stale;
	}

	public synchronized void removeChangeListener(IChangeListener listener) {
		changeListeners.remove(listener);
	}

	public synchronized void removeStaleListener(IStaleListener listener) {
		staleListeners.remove(listener);
	}

	/**
	 * Sets the stale state.  Must be invoked from the current realm.
	 * 
	 * @param stale
	 */
	public void setStale(boolean stale) {
		checkRealm();
		this.stale = stale;
		if (stale) {
			fireStale();
		}
	}

	/**
	 * Fires stale events.  Must be invoked from current realm.
	 */
	protected void fireStale() {
		checkRealm();
		Object[] listeners = staleListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IStaleListener listener = (IStaleListener) listeners[i];
			listener.handleStale(this);
		}
	}

	/**
	 * Fires change events.  Must be invoked from current realm.
	 */
	protected void fireChange() {
		checkRealm();
		Object[] listeners = changeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IChangeListener listener = (IChangeListener) listeners[i];
			listener.handleChange(this);
		}
	}

	/**
	 * Fires map change events.  Must be invoked from current realm.
	 * 
	 * @param diff
	 */
	protected void fireMapChange(MapDiff diff) {
		checkRealm();
		Object[] listeners = mapListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IMapChangeListener listener = (IMapChangeListener) listeners[i];
			listener.handleMapChange(this, diff);
		}
	}

	/**
	 * Asserts that the realm is the current realm.
	 * 
	 * @see Realm#isCurrent()
	 * @throws AssertionFailedException
	 *             if the realm is not the current realm
	 */
	protected void checkRealm() {
		Assert.isTrue(realm.isCurrent());
	}
}
