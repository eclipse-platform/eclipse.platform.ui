/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.AbstractMap;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.ChangeSupport;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 * @since 3.3
 * 
 */
public abstract class AbstractObservableMap extends AbstractMap implements
		IObservableMap {

	private ChangeSupport changeSupport;

	private boolean stale;

	/**
	 */
	public AbstractObservableMap() {
		this(Realm.getDefault());
	}

	/**
	 * 
	 */
	protected void lastListenerRemoved() {
	}

	/**
	 * 
	 */
	protected void firstListenerAdded() {
	}

	/**
	 * @param realm
	 */
	public AbstractObservableMap(Realm realm) {
		Assert.isNotNull(realm);
		changeSupport = new ChangeSupport(realm){
			protected void firstListenerAdded() {
				AbstractObservableMap.this.firstListenerAdded();
			}
			protected void lastListenerRemoved() {
				AbstractObservableMap.this.lastListenerRemoved();
			}
		};
	}

	public synchronized void addMapChangeListener(IMapChangeListener listener) {
		changeSupport.addListener(MapChangeEvent.TYPE, listener);
	}

	public synchronized void removeMapChangeListener(IMapChangeListener listener) {
		changeSupport.removeListener(MapChangeEvent.TYPE, listener);
	}

	public synchronized void addChangeListener(IChangeListener listener) {
		changeSupport.addChangeListener(listener);
	}

	public synchronized void addStaleListener(IStaleListener listener) {
		changeSupport.addStaleListener(listener);
	}

	public synchronized void dispose() {
		changeSupport.dispose();
		changeSupport = null;
	}

	public Realm getRealm() {
		return changeSupport.getRealm();
	}

	public boolean isStale() {
		checkRealm();
		return stale;
	}

	public synchronized void removeChangeListener(IChangeListener listener) {
		changeSupport.removeChangeListener(listener);
	}

	public synchronized void removeStaleListener(IStaleListener listener) {
		changeSupport.removeStaleListener(listener);
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
		changeSupport.fireEvent(new StaleEvent(this));
	}

	/**
	 * Fires change events.  Must be invoked from current realm.
	 */
	protected void fireChange() {
		checkRealm();
		changeSupport.fireEvent(new ChangeEvent(this));
	}

	/**
	 * Fires map change events.  Must be invoked from current realm.
	 * 
	 * @param diff
	 */
	protected void fireMapChange(MapDiff diff) {
		checkRealm();
		changeSupport.fireEvent(new MapChangeEvent(this, diff));
	}

	/**
	 * Asserts that the realm is the current realm.
	 * 
	 * @see Realm#isCurrent()
	 * @throws AssertionFailedException
	 *             if the realm is not the current realm
	 */
	protected void checkRealm() {
		Assert.isTrue(getRealm().isCurrent());
	}
}
