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

package org.eclipse.jface.internal.databinding.provisional.viewers;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.databinding.observable.IChangeListener;
import org.eclipse.jface.databinding.observable.IStaleListener;
import org.eclipse.jface.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.observable.set.ISetChangeListener;

/**
 * Singleton empty set
 */
public class EmptyObservableSet implements IObservableSet {

	private static final Set emptySet = Collections.EMPTY_SET;
	private static EmptyObservableSet instance;
	
	private EmptyObservableSet() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#addSetChangeListener(org.eclipse.jface.internal.databinding.provisional.observable.set.ISetChangeListener)
	 */
	public void addSetChangeListener(ISetChangeListener listener) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#removeSetChangeListener(org.eclipse.jface.internal.databinding.provisional.observable.set.ISetChangeListener)
	 */
	public void removeSetChangeListener(ISetChangeListener listener) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#getElementType()
	 */
	public Object getElementType() {
		// TODO: What is this supposed to be for?
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#size()
	 */
	public int size() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#isEmpty()
	 */
	public boolean isEmpty() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#iterator()
	 */
	public Iterator iterator() {
		return emptySet.iterator();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#toArray()
	 */
	public Object[] toArray() {
		return emptySet.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#toArray(java.lang.Object[])
	 */
	public Object[] toArray(Object[] a) {
		return emptySet.toArray(a);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#add(java.lang.Object)
	 */
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection c) {
		return c.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.set.IObservableSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#clear()
	 */
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#addChangeListener(org.eclipse.jface.internal.databinding.provisional.observable.IChangeListener)
	 */
	public void addChangeListener(IChangeListener listener) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#removeChangeListener(org.eclipse.jface.internal.databinding.provisional.observable.IChangeListener)
	 */
	public void removeChangeListener(IChangeListener listener) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#addStaleListener(org.eclipse.jface.internal.databinding.provisional.observable.IStaleListener)
	 */
	public void addStaleListener(IStaleListener listener) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#removeStaleListener(org.eclipse.jface.internal.databinding.provisional.observable.IStaleListener)
	 */
	public void removeStaleListener(IStaleListener listener) {}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#isStale()
	 */
	public boolean isStale() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.internal.databinding.provisional.observable.IObservable#dispose()
	 */
	public void dispose() {}

	/**
	 * Returns the singleton empty set. This set may be disposed multiple times without any
	 * side-effects.
	 * 
	 * @return the singleton empty set.
	 */
	public static IObservableSet getInstance() {
		if (instance == null) {
			instance = new EmptyObservableSet();			
		}

		return instance;
	}
	
}
