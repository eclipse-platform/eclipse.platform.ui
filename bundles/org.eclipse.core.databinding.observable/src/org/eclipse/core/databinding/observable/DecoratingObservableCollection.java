/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

import java.util.Collection;
import java.util.Iterator;

/**
 * An observable collection which decorates another observable collection
 *
 * @param <E>
 *
 * @since 1.2
 */
public class DecoratingObservableCollection<E> extends DecoratingObservable implements IObservableCollection<E> {
	private IObservableCollection<E> decorated;

	/**
	 * @param decorated
	 * @param disposeDecoratedOnDispose
	 */
	public DecoratingObservableCollection(IObservableCollection<E> decorated,
			boolean disposeDecoratedOnDispose) {
		super(decorated, disposeDecoratedOnDispose);
		this.decorated = decorated;
	}

	@Override
        public boolean add(E o) {
		getterCalled();
		return decorated.add(o);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		getterCalled();
		return decorated.addAll(c);
	}

	@Override
	public void clear() {
		checkRealm();
		decorated.clear();
	}

	@Override
	public boolean contains(Object o) {
		getterCalled();
		return decorated.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		getterCalled();
		return decorated.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		getterCalled();
		return decorated.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		getterCalled();
		final Iterator<E> decoratedIterator = decorated.iterator();
		return new Iterator<E>() {
			@Override
			public void remove() {
				decoratedIterator.remove();
			}

			@Override
			public boolean hasNext() {
				getterCalled();
				return decoratedIterator.hasNext();
			}

			@Override
			public E next() {
				getterCalled();
				return decoratedIterator.next();
			}
		};
	}

	@Override
	public boolean remove(Object o) {
		getterCalled();
		return decorated.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		getterCalled();
		return decorated.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		getterCalled();
		return decorated.retainAll(c);
	}

	@Override
	public int size() {
		getterCalled();
		return decorated.size();
	}

	@Override
	public Object[] toArray() {
		getterCalled();
		return decorated.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		getterCalled();
		return decorated.toArray(a);
	}

	@Override
	public Object getElementType() {
		return decorated.getElementType();
	}

	@Override
	public boolean equals(Object obj) {
		getterCalled();
		if (this == obj) {
			return true;
		}
		return decorated.equals(obj);
	}

	@Override
	public int hashCode() {
		getterCalled();
		return decorated.hashCode();
	}

	@Override
	public String toString() {
		getterCalled();
		return decorated.toString();
	}

	@Override
	public synchronized void dispose() {
		decorated = null;
		super.dispose();
	}
}
