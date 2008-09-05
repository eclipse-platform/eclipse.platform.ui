/*******************************************************************************
 * Copyright (c) 2006-2008 Cerner Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 208332, 237718
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.list.DecoratingObservableList;
import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * ObservableList implementation that prevents modification by consumers. Events
 * in the originating wrapped list are propagated and thrown from this instance
 * when appropriate.  All mutators throw an UnsupportedOperationException.
 * 
 * @since 1.0
 */
public class UnmodifiableObservableList extends DecoratingObservableList {
	private List unmodifiableList;

	/**
	 * @param decorated
	 */
	public UnmodifiableObservableList(IObservableList decorated) {
		super(decorated, false);
		this.unmodifiableList = Collections.unmodifiableList(decorated);
	}

	public void add(int index, Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(int index, Collection c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public Iterator iterator() {
		getterCalled();
		return unmodifiableList.iterator();
	}

	public ListIterator listIterator() {
		return listIterator(0);
	}

	public ListIterator listIterator(int index) {
		getterCalled();
		return unmodifiableList.listIterator(index);
	}

	public Object move(int oldIndex, int newIndex) {
		throw new UnsupportedOperationException();
	}

	public Object remove(int index) {
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

	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	public List subList(int fromIndex, int toIndex) {
		getterCalled();
		return unmodifiableList.subList(fromIndex, toIndex);
	}

	public synchronized void dispose() {
		unmodifiableList = null;
		super.dispose();
	}
}
