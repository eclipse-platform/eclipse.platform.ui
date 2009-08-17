/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 208858, 208332, 146397, 249526
 *******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.runtime.Assert;

/**
 * Singleton empty list
 */
public class EmptyObservableList implements IObservableList {

	private static final List emptyList = Collections.EMPTY_LIST;

	private final Realm realm;
	private Object elementType;

	/**
	 * Creates an empty list. This list may be disposed multiple times
	 * without any side-effects.
	 * 
	 * @param realm
	 *            the realm of the constructed list
	 */
	public EmptyObservableList(Realm realm) {
		this(realm, null);
	}

	/**
	 * Creates an empty list. This list may be disposed multiple times
	 * without any side-effects.
	 * 
	 * @param realm
	 *            the realm of the constructed list
	 * @param elementType
	 *            the element type of the constructed list
	 * @since 1.1
	 */
	public EmptyObservableList(Realm realm, Object elementType) {
		this.realm = realm;
		this.elementType = elementType;
		ObservableTracker.observableCreated(this);
	}

	public void addListChangeListener(IListChangeListener listener) {
		// ignore
	}

	public void removeListChangeListener(IListChangeListener listener) {
		// ignore
	}

	public Object getElementType() {
		return elementType;
	}

	public int size() {
		checkRealm();
		return 0;
	}

	void checkRealm() {
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
		return emptyList.iterator();
	}

	public Object[] toArray() {
		checkRealm();
		return emptyList.toArray();
	}

	public Object[] toArray(Object[] a) {
		return emptyList.toArray(a);
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

	public boolean addAll(int index, Collection c) {
		throw new UnsupportedOperationException();
	}

	public Object get(int index) {
		return emptyList.get(index);
	}

	public int indexOf(Object o) {
		return -1;
	}

	public int lastIndexOf(Object o) {
		return -1;
	}

	public ListIterator listIterator() {
		return emptyList.listIterator();
	}

	public ListIterator listIterator(int index) {
		return emptyList.listIterator(index);
	}

	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	public Object move(int oldIndex, int newIndex) {
		throw new UnsupportedOperationException();
	}

	public List subList(int fromIndex, int toIndex) {
		return emptyList.subList(fromIndex, toIndex);
	}

	public void add(int index, Object o) {
		throw new UnsupportedOperationException();
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
		if (!(obj instanceof List))
			return false;

		return ((List) obj).isEmpty();
	}

	public int hashCode() {
		checkRealm();
		return 1;
	}
}
