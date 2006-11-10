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

package org.eclipse.core.internal.databinding.observable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;

/**
 * Singleton empty set
 */
public class EmptyObservableSet implements IObservableSet {

	private static final Set emptySet = Collections.EMPTY_SET;

	private Realm realm;

	/**
	 * Creates a singleton empty set. This set may be disposed multiple times
	 * without any side-effects.
	 * 
	 * @param realm
	 */
	public EmptyObservableSet(Realm realm) {
		this.realm = realm;
	}

	public void addSetChangeListener(ISetChangeListener listener) {
	}

	public void removeSetChangeListener(ISetChangeListener listener) {
	}

	public Object getElementType() {
		return Object.class;
	}

	public int size() {
		return 0;
	}

	public boolean isEmpty() {
		return true;
	}

	public boolean contains(Object o) {
		return false;
	}

	public Iterator iterator() {
		return emptySet.iterator();
	}

	public Object[] toArray() {
		return emptySet.toArray();
	}

	public Object[] toArray(Object[] a) {
		return emptySet.toArray(a);
	}

	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean containsAll(Collection c) {
		return c.isEmpty();
	}

	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public void addChangeListener(IChangeListener listener) {
	}

	public void removeChangeListener(IChangeListener listener) {
	}

	public void addStaleListener(IStaleListener listener) {
	}

	public void removeStaleListener(IStaleListener listener) {
	}

	public boolean isStale() {
		return false;
	}

	public void dispose() {
	}

	public Realm getRealm() {
		return realm;
	}

}
