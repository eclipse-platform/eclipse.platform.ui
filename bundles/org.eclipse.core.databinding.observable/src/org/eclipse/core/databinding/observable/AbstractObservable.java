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

package org.eclipse.core.databinding.observable;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 * @since 1.0
 */
public abstract class AbstractObservable implements IObservable {

	/**
	 * Points to an instance of IChangeListener or a Collection of
	 * IChangeListener.  Access must be synchronized.
	 */
	private Object changeListeners = null;

	/**
	 * Points to an instance of IChangeListener or a Collection of
	 * IChangeListener.  Access must be synchronized.
	 */
	private Object staleListeners = null;
	
	private Realm realm;

	/**
	 * @param realm
	 */
	public AbstractObservable(Realm realm) {
		Assert.isNotNull(realm);
		this.realm = realm;
	}

	public synchronized void addChangeListener(IChangeListener listener) {
		if (changeListeners == null) {
			boolean hadListeners = hasListeners();
			changeListeners = listener;
			if (!hadListeners) {
				firstListenerAdded();
			}
			return;
		}

		Collection listenerList;
		if (changeListeners instanceof IChangeListener) {
			IChangeListener l = (IChangeListener) changeListeners;

			listenerList = new ArrayList();
			listenerList.add(l);
			changeListeners = listenerList;
		} else {
			listenerList = (Collection) changeListeners;
		}

		listenerList.add(listener);
	}

	public synchronized void removeChangeListener(IChangeListener listener) {
		if (changeListeners == listener) {
			changeListeners = null;
			if (!hasListeners()) {
				lastListenerRemoved();
			}
			return;
		}

		if (changeListeners instanceof Collection) {
			Collection listenerList = (Collection) changeListeners;
			listenerList.remove(listener);
			if (listenerList.isEmpty()) {
				changeListeners = null;
				if (!hasListeners()) {
					lastListenerRemoved();
				}
			}
		}
	}

	public synchronized void addStaleListener(IStaleListener listener) {
		if (staleListeners == null) {
			boolean hadListeners = hasListeners();
			staleListeners = listener;
			if (!hadListeners) {
				firstListenerAdded();
			}
			return;
		}

		Collection listenerList;
		if (staleListeners instanceof IStaleListener) {
			IStaleListener l = (IStaleListener) staleListeners;

			listenerList = new ArrayList();
			listenerList.add(l);
			staleListeners = listenerList;
		} else {
			listenerList = (Collection) staleListeners;
		}

		listenerList.add(listener);
	}

	public synchronized void removeStaleListener(IStaleListener listener) {
		if (staleListeners == listener) {
			staleListeners = null;
			if (!hasListeners()) {
				lastListenerRemoved();
			}
			return;
		}

		if (staleListeners instanceof Collection) {
			Collection listenerList = (Collection) staleListeners;
			listenerList.remove(listener);
			if (listenerList.isEmpty()) {
				staleListeners = null;
				if (!hasListeners()) {
					lastListenerRemoved();
				}
			}
		}
	}

	protected void fireChange() {
		checkRealm();
		
		if (changeListeners == null) {
			return;
		}

		if (changeListeners instanceof IChangeListener) {
			((IChangeListener) changeListeners).handleChange(this);
			return;
		}

		Collection changeListenerCollection = (Collection) changeListeners;

		IChangeListener[] listeners = (IChangeListener[]) (changeListenerCollection)
				.toArray(new IChangeListener[changeListenerCollection.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleChange(this);
		}
	}

	protected void fireStale() {
		checkRealm();
		
		if (staleListeners == null) {
			return;
		}

		if (staleListeners instanceof IStaleListener) {
			((IStaleListener) staleListeners).handleStale(this);
			return;
		}

		Collection staleListenerCollection = (Collection) staleListeners;

		IStaleListener[] listeners = (IStaleListener[]) (staleListenerCollection)
				.toArray(new IStaleListener[staleListenerCollection.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleStale(this);
		}
	}

	/**
	 * @return true if this observable has listeners
	 */
	protected synchronized boolean hasListeners() {
		return changeListeners != null || staleListeners != null;
	}

	/**
	 * 
	 */
	protected void firstListenerAdded() {
	}

	/**
	 * 
	 */
	protected void lastListenerRemoved() {
	}

	/**
	 * 
	 */
	public synchronized void dispose() {
		changeListeners = null;
		staleListeners = null;
	}

	/**
	 * @return Returns the realm.
	 */
	public Realm getRealm() {
		return realm;
	}
	
	/**
	 * Checks the current realm for the current realm.
	 * 
	 * @throws AssertionFailedException if the realm is not the current realm
	 */
	protected void checkRealm() {
		Assert.isTrue(realm.isCurrent());
	}
}
