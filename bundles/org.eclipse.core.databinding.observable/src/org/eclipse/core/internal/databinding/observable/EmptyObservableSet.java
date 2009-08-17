/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 208332, 146397, 249526
 *******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.runtime.Assert;

/**
 * Singleton empty set
 */
public class EmptyObservableSet implements IObservableSet {

	private static final Set emptySet = Collections.EMPTY_SET;

	private final Realm realm;
	private Object elementType;

	/**
	 * Creates a singleton empty set. This set may be disposed multiple times
	 * without any side-effects.
	 * 
	 * @param realm
	 *            the realm of the constructed set
	 */
	public EmptyObservableSet(Realm realm) {
		this(realm, null);
	}

	/**
	 * Creates a singleton empty set. This set may be disposed multiple times
	 * without any side-effects.
	 * 
	 * @param realm
	 *            the realm of the constructed set
	 * @param elementType
	 *            the element type of the constructed set
	 * @since 1.1
	 */
	public EmptyObservableSet(Realm realm, Object elementType) {
		this.realm = realm;
		this.elementType = elementType;
		ObservableTracker.observableCreated(this);
	}

	public void addSetChangeListener(ISetChangeListener listener) {
	}

	public void removeSetChangeListener(ISetChangeListener listener) {
	}

	public Object getElementType() {
		return elementType;
	}

	public int size() {
		checkRealm();
		return 0;
	}

	private void checkRealm() {
		Assert.isTrue(realm.isCurrent(),
				"Observable cannot be accessed outside its realm"); //$NON-NLS-1$
	}

	public boolean isEmpty() {
		checkRealm();
		return true;
	}

	public boolean contains(Object o) {
		checkRealm();
		return false;
	}

	public Iterator iterator() {
		checkRealm();
		return emptySet.iterator();
	}

	public Object[] toArray() {
		checkRealm();
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
		checkRealm();
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

	public void addDisposeListener(IDisposeListener listener) {
	}

	public void removeDisposeListener(IDisposeListener listener) {
	}

	public boolean isStale() {
		checkRealm();
		return false;
	}

	public boolean isDisposed() {
		return false;
	}

	public void dispose() {
	}

	public Realm getRealm() {
		return realm;
	}

	public boolean equals(Object obj) {
		checkRealm();
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Set))
			return false;

		return ((Set) obj).isEmpty();
	}

	public int hashCode() {
		checkRealm();
		return 0;
	}
}
