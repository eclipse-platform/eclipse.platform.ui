/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

import java.util.Collection;
import java.util.Iterator;

/**
 * An observable collection which decorates another observable collection
 * 
 * @since 1.2
 */
public class DecoratingObservableCollection extends DecoratingObservable
		implements IObservableCollection {
	private IObservableCollection decorated;

	/**
	 * @param decorated
	 * @param disposeDecoratedOnDispose
	 */
	public DecoratingObservableCollection(IObservableCollection decorated,
			boolean disposeDecoratedOnDispose) {
		super(decorated, disposeDecoratedOnDispose);
		this.decorated = decorated;
	}

	public boolean add(Object o) {
		getterCalled();
		return decorated.add(o);
	}

	public boolean addAll(Collection c) {
		getterCalled();
		return decorated.addAll(c);
	}

	public void clear() {
		checkRealm();
		decorated.clear();
	}

	public boolean contains(Object o) {
		getterCalled();
		return decorated.contains(o);
	}

	public boolean containsAll(Collection c) {
		getterCalled();
		return decorated.containsAll(c);
	}

	public boolean isEmpty() {
		getterCalled();
		return decorated.isEmpty();
	}

	public Iterator iterator() {
		getterCalled();
		final Iterator decoratedIterator = decorated.iterator();
		return new Iterator() {
			public void remove() {
				decoratedIterator.remove();
			}

			public boolean hasNext() {
				getterCalled();
				return decoratedIterator.hasNext();
			}

			public Object next() {
				getterCalled();
				return decoratedIterator.next();
			}
		};
	}

	public boolean remove(Object o) {
		getterCalled();
		return decorated.remove(o);
	}

	public boolean removeAll(Collection c) {
		getterCalled();
		return decorated.removeAll(c);
	}

	public boolean retainAll(Collection c) {
		getterCalled();
		return decorated.retainAll(c);
	}

	public int size() {
		getterCalled();
		return decorated.size();
	}

	public Object[] toArray() {
		getterCalled();
		return decorated.toArray();
	}

	public Object[] toArray(Object[] a) {
		getterCalled();
		return decorated.toArray(a);
	}

	public Object getElementType() {
		return decorated.getElementType();
	}

	public boolean equals(Object obj) {
		getterCalled();
		if (this == obj) {
			return true;
		}
		return decorated.equals(obj);
	}

	public int hashCode() {
		getterCalled();
		return decorated.hashCode();
	}

	public String toString() {
		getterCalled();
		return decorated.toString();
	}

	public synchronized void dispose() {
		decorated = null;
		super.dispose();
	}
}
