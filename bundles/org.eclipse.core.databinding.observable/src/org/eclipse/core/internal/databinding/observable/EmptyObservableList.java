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

	@Override
	public void addListChangeListener(IListChangeListener listener) {
		// ignore
	}

	@Override
	public void removeListChangeListener(IListChangeListener listener) {
		// ignore
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	public int size() {
		checkRealm();
		return 0;
	}

	void checkRealm() {
		Assert.isTrue(realm.isCurrent(),
				"Observable cannot be accessed outside its realm"); //$NON-NLS-1$
	}

	@Override
	public boolean isEmpty() {
		checkRealm();
		return true;
	}

	@Override
	public boolean contains(Object o) {
		checkRealm();
		return false;
	}

	@Override
	public Iterator iterator() {
		checkRealm();
		return emptyList.iterator();
	}

	@Override
	public Object[] toArray() {
		checkRealm();
		return emptyList.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return emptyList.toArray(a);
	}

	@Override
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection c) {
		checkRealm();
		return c.isEmpty();
	}

	@Override
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addChangeListener(IChangeListener listener) {
	}

	@Override
	public void removeChangeListener(IChangeListener listener) {
	}

	@Override
	public void addStaleListener(IStaleListener listener) {
	}

	@Override
	public void removeStaleListener(IStaleListener listener) {
	}

	@Override
	public void addDisposeListener(IDisposeListener listener) {
	}

	@Override
	public void removeDisposeListener(IDisposeListener listener) {
	}

	@Override
	public boolean isStale() {
		checkRealm();
		return false;
	}

	@Override
	public boolean isDisposed() {
		return false;
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean addAll(int index, Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object get(int index) {
		return emptyList.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		return -1;
	}

	@Override
	public ListIterator listIterator() {
		return emptyList.listIterator();
	}

	@Override
	public ListIterator listIterator(int index) {
		return emptyList.listIterator(index);
	}

	@Override
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object move(int oldIndex, int newIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return emptyList.subList(fromIndex, toIndex);
	}

	@Override
	public void add(int index, Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Realm getRealm() {
		return realm;
	}

	@Override
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

	@Override
	public int hashCode() {
		checkRealm();
		return 1;
	}
}
