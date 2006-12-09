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

package org.eclipse.core.databinding.observable.list;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;

/**
 * Subclasses should override at least get(int index) and size().
 * 
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * 
 * @since 1.0
 * 
 */
public abstract class AbstractObservableList extends AbstractList implements
		IObservableList {

	/**
	 * Points to an instance of IListChangeListener or a Collection of
	 * IListChangeListener.  Access must be synchronized.
	 */
	private Object listChangeListeners;

	/**
	 * Points to an instance of IChangeListener or a Collection of
	 * IChangeListener.  Access must be synchronized.
	 */
	private Object changeListeners = null;

	/**
	 * Points to an instance of IChangeListener or a Collection of
	 * IStaleListener.  Access must be synchronized.
	 */
	private Object staleListeners = null;

	private Realm realm;
	
	/**
	 * @param realm 
	 * 
	 */
	public AbstractObservableList(Realm realm) {
		Assert.isNotNull(realm);
		this.realm = realm;
	}

	/**
	 * 
	 */
	public AbstractObservableList() {
		this(Realm.getDefault());
	}
	
	public boolean isStale() {
		return false;
	}

	public synchronized void addListChangeListener(IListChangeListener listener) {
		if (listChangeListeners == null) {
			boolean hadListeners = hasListeners();
			listChangeListeners = listener;
			if (!hadListeners) {
				firstListenerAdded();
			}
			return;
		}

		Collection listenerList;
		if (listChangeListeners instanceof Collection) {
			listenerList = (Collection) listChangeListeners;
		} else {
			IListChangeListener l = (IListChangeListener) listChangeListeners;

			listenerList = new ArrayList();
			listenerList.add(l);
			listChangeListeners = listenerList;
		}

		listenerList.add(listener);
	}

	public synchronized void removeListChangeListener(IListChangeListener listener) {

		if (listChangeListeners == listener) {
			listChangeListeners = null;
			if (!hasListeners()) {
				lastListenerRemoved();
			}
			return;
		}

		if (listChangeListeners instanceof Collection) {
			Collection listenerList = (Collection) listChangeListeners;
			listenerList.remove(listener);
			if (listenerList.isEmpty()) {
				listChangeListeners = null;
				if (!hasListeners()) {
					lastListenerRemoved();
				}
			}
		}
	}

	protected void fireListChange(ListDiff diff) {
		// fire general change event first
		fireChange();

		if (listChangeListeners == null) {
			return;
		}

		if (listChangeListeners instanceof IListChangeListener) {
			((IListChangeListener) listChangeListeners).handleListChange(this,
					diff);
			return;
		}

		Collection changeListenerCollection = (Collection) listChangeListeners;

		IListChangeListener[] listeners = (IListChangeListener[]) (changeListenerCollection)
				.toArray(new IListChangeListener[changeListenerCollection
						.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleListChange(this, diff);
		}
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

	/**
	 * Fires change event. Must be invoked from the current realm.
	 */
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

	/**
	 * Fires stale event. Must be invoked from the current realm.
	 */
	protected void fireStale() {
		checkRealm();

		if (staleListeners == null) {
			return;
		}

		if (staleListeners instanceof IChangeListener) {
			((IChangeListener) staleListeners).handleChange(this);
			return;
		}

		Collection changeListenerCollection = (Collection) staleListeners;

		IChangeListener[] listeners = (IChangeListener[]) (changeListenerCollection)
				.toArray(new IChangeListener[changeListenerCollection.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleChange(this);
		}
	}

	/**
	 * @return true if this observable has listeners
	 */
	protected synchronized boolean hasListeners() {
		return changeListeners != null || staleListeners != null
				|| listChangeListeners != null;
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
		listChangeListeners = null;
		changeListeners = null;
		staleListeners = null;
		lastListenerRemoved();
	}

	public final int size() {
		getterCalled();
		return doGetSize();
	}

	/**
	 * @return the size
	 */
	protected abstract int doGetSize();

	/**
	 * 
	 */
	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	public boolean isEmpty() {
		getterCalled();
		return super.isEmpty();
	}

	public boolean contains(Object o) {
		getterCalled();
		return super.contains(o);
	}

	public Iterator iterator() {
		final Iterator wrappedIterator = super.iterator();
		return new Iterator() {
			public void remove() {
				wrappedIterator.remove();
			}

			public boolean hasNext() {
				getterCalled();
				return wrappedIterator.hasNext();
			}

			public Object next() {
				getterCalled();
				return wrappedIterator.next();
			}
		};
	}

	public Object[] toArray() {
		getterCalled();
		return super.toArray();
	}

	public Object[] toArray(Object a[]) {
		getterCalled();
		return super.toArray(a);
	}

	// Modification Operations

	public boolean add(Object o) {
		getterCalled();
		return super.add(o);
	}

	public boolean remove(Object o) {
		getterCalled();
		return super.remove(o);
	}

	// Bulk Modification Operations

	public boolean containsAll(Collection c) {
		getterCalled();
		return super.containsAll(c);
	}

	public boolean addAll(Collection c) {
		getterCalled();
		return super.addAll(c);
	}

	public boolean addAll(int index, Collection c) {
		getterCalled();
		return super.addAll(c);
	}

	public boolean removeAll(Collection c) {
		getterCalled();
		return super.removeAll(c);
	}

	public boolean retainAll(Collection c) {
		getterCalled();
		return super.retainAll(c);
	}

	// Comparison and hashing

	public boolean equals(Object o) {
		getterCalled();
		return super.equals(o);
	}

	public int hashCode() {
		getterCalled();
		return super.hashCode();
	}

	public int indexOf(Object o) {
		getterCalled();
		return super.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		getterCalled();
		return super.lastIndexOf(o);
	}

	public Realm getRealm() {
		return realm;
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
