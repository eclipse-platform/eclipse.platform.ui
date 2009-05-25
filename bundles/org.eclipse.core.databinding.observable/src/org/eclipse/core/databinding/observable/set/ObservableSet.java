/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 208332, 274450
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;

/**
 * 
 * Abstract implementation of {@link IObservableSet}.
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
public abstract class ObservableSet extends AbstractObservable implements
		IObservableSet {

	protected Set wrappedSet;

	private boolean stale = false;

	protected Object elementType;

	protected ObservableSet(Set wrappedSet, Object elementType) {
		this(Realm.getDefault(), wrappedSet, elementType);
	}

	protected ObservableSet(Realm realm, Set wrappedSet, Object elementType) {
		super(realm);
		this.wrappedSet = wrappedSet;
		this.elementType = elementType;
	}

	public synchronized void addSetChangeListener(ISetChangeListener listener) {
		addListener(SetChangeEvent.TYPE, listener);
	}

	public synchronized void removeSetChangeListener(ISetChangeListener listener) {
		removeListener(SetChangeEvent.TYPE, listener);
	}

	protected void fireSetChange(SetDiff diff) {
		// fire general change event first
		super.fireChange();

		fireEvent(new SetChangeEvent(this, diff));
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
		return o == this || wrappedSet.equals(o);
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
		getterCalled();
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
		getterCalled();
		return stale;
	}

	/**
	 * @param stale
	 *            The stale state to set. This will fire a stale event if the
	 *            given boolean is true and this observable set was not already
	 *            stale.
	 */
	public void setStale(boolean stale) {
		checkRealm();
		boolean wasStale = this.stale;
		this.stale = stale;
		if (!wasStale && stale) {
			fireStale();
		}
	}

	/**
	 * @param wrappedSet
	 *            The wrappedSet to set.
	 */
	protected void setWrappedSet(Set wrappedSet) {
		this.wrappedSet = wrappedSet;
	}

	protected void fireChange() {
		throw new RuntimeException(
				"fireChange should not be called, use fireSetChange() instead"); //$NON-NLS-1$
	}

	public synchronized void dispose() {
		super.dispose();
	}

	public Object getElementType() {
		return elementType;
	}
}
