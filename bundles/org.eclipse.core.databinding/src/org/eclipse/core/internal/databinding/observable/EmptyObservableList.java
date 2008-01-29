/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 208858
 *******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * Singleton empty list
 */
public class EmptyObservableList implements IObservableList {

	private static final List emptyList = Collections.EMPTY_LIST;

	private Realm realm;

	/**
	 * Creates a singleton empty list. This list may be disposed multiple times
	 * without any side-effects.
	 * 
	 * @param realm
	 */
	public EmptyObservableList(Realm realm) {
		this.realm = realm;
	}

	public void addListChangeListener(IListChangeListener listener) {
	}

	public void removeListChangeListener(IListChangeListener listener) {
	}

	public Object getElementType() {
		return null;
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
		return emptyList.iterator();
	}

	public Object[] toArray() {
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

	public void add(int arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	public Realm getRealm() {
		return realm;
	}

}
