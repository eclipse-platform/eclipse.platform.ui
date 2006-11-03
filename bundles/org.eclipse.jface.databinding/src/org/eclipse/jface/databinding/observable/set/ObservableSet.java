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

package org.eclipse.jface.databinding.observable.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.databinding.observable.AbstractObservable;
import org.eclipse.jface.databinding.observable.ObservableTracker;
import org.eclipse.jface.databinding.observable.Realm;

/**
 * 
 * Abstract implementation of {@link IObservableSet}. 
 * 
 * @since 1.0
 * 
 */
public abstract class ObservableSet extends AbstractObservable implements
		IObservableSet {

	protected Set wrappedSet;

	private boolean stale = false;

	private Object setChangeListeners;

	protected Object elementType;

	protected ObservableSet(Set wrappedSet, Object elementType) {
		this(Realm.getDefault(), wrappedSet, elementType);
	}

	protected ObservableSet(Realm realm, Set wrappedSet, Object elementType) {
		super(realm);
		this.wrappedSet = wrappedSet;
		this.elementType = elementType;
	}
	
	public void addSetChangeListener(ISetChangeListener listener) {
		if (setChangeListeners == null) {
			boolean hadListeners = hasListeners();
			setChangeListeners = listener;
			if (!hadListeners) {
				firstListenerAdded();
			}
			return;
		}

		Collection listenerList;
		if (setChangeListeners instanceof Collection) {
			listenerList = (Collection) setChangeListeners;
		} else {
			ISetChangeListener l = (ISetChangeListener) setChangeListeners;
			
			listenerList = new ArrayList();
			listenerList.add(l);
			setChangeListeners = listenerList;
		}

		listenerList.add(listener);
	}

	public void removeSetChangeListener(ISetChangeListener listener) {

		if (setChangeListeners == listener) {
			setChangeListeners = null;
			if (!hasListeners()) {
				lastListenerRemoved();
			}
			return;
		}

		if (setChangeListeners instanceof Collection) {
			Collection listenerList = (Collection) setChangeListeners;
			listenerList.remove(listener);
			if (listenerList.isEmpty()) {
				setChangeListeners = null;
				if (!hasListeners()) {
					lastListenerRemoved();
				}
			}
		}
	}

	protected boolean hasListeners() {
		return super.hasListeners() || setChangeListeners!=null;
	}

	protected void fireSetChange(SetDiff diff) {
		// fire general change event first
		super.fireChange();

		if (setChangeListeners == null) {
			return;
		}
		
		if (setChangeListeners instanceof ISetChangeListener) {
			((ISetChangeListener) setChangeListeners).handleSetChange(this, diff);
			return;
		}
		
		Collection changeListenerCollection = (Collection) setChangeListeners;
		
		ISetChangeListener[] listeners = (ISetChangeListener[]) (changeListenerCollection)
		.toArray(new ISetChangeListener[changeListenerCollection.size()]);
		for (int i = 0; i < listeners.length; i++) {
			listeners[i].handleSetChange(this, diff);
		}
	}
	
	public boolean contains(Object o) {
		getterCalled();
		return wrappedSet.contains(o);
	}

	public boolean containsAll(Collection c) {
		getterCalled();
		return wrappedSet.containsAll(c);
	}

	public boolean equals(Object o) {
		getterCalled();
		return wrappedSet.equals(o);
	}

	public int hashCode() {
		getterCalled();
		return wrappedSet.hashCode();
	}

	public boolean isEmpty() {
		getterCalled();
		return wrappedSet.isEmpty();
	}

	public Iterator iterator() {
		final Iterator wrappedIterator = wrappedSet.iterator();
		return new Iterator() {

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public boolean hasNext() {
				ObservableTracker.getterCalled(ObservableSet.this);
				return wrappedIterator.hasNext();
			}

			public Object next() {
				ObservableTracker.getterCalled(ObservableSet.this);
				return wrappedIterator.next();
			}
		};
	}

	public int size() {
		getterCalled();
		return wrappedSet.size();
	}

	public Object[] toArray() {
		getterCalled();
		return wrappedSet.toArray();
	}

	public Object[] toArray(Object[] a) {
		getterCalled();
		return wrappedSet.toArray(a);
	}

	public String toString() {
		getterCalled();
		return wrappedSet.toString();
	}

	protected void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
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

	/**
	 * @param wrappedSet The wrappedSet to set.
	 */
	protected void setWrappedSet(Set wrappedSet) {
		this.wrappedSet = wrappedSet;
	}

	protected void fireChange() {
		throw new RuntimeException("fireChange should not be called, use fireSetChange() instead"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.provisional.databinding.observable.AbstractObservable#dispose()
	 */
	public void dispose() {
		setChangeListeners = null;
		super.dispose();
	}
	
	public Object getElementType() {
		return elementType;
	}
}
