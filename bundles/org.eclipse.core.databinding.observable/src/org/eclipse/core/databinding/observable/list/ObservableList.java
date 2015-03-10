/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 164653, 167204
 *     Matthew Hall - bugs 208858, 208332, 274450
 *******************************************************************************/

package org.eclipse.core.databinding.observable.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;

/**
 *
 * Abstract implementation of {@link IObservableList}, based on an underlying
 * regular list.
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 *
 * @since 1.0
 *
 */
public abstract class ObservableList extends AbstractObservable implements
		IObservableList {

	protected List wrappedList;

	/**
	 * Stale state of the list. Access must occur in the current realm.
	 */
	private boolean stale = false;

	private Object elementType;

	protected ObservableList(List wrappedList, Object elementType) {
		this(Realm.getDefault(), wrappedList, elementType);
	}

	protected ObservableList(Realm realm, List wrappedList, Object elementType) {
		super(realm);
		this.wrappedList = wrappedList;
		this.elementType = elementType;
	}

	@Override
	public synchronized void addListChangeListener(IListChangeListener listener) {
		addListener(ListChangeEvent.TYPE, listener);
	}

	@Override
	public synchronized void removeListChangeListener(
			IListChangeListener listener) {
		removeListener(ListChangeEvent.TYPE, listener);
	}

	protected void fireListChange(ListDiff diff) {
		// fire general change event first
		super.fireChange();
		fireEvent(new ListChangeEvent(this, diff));
	}

	@Override
	public boolean contains(Object o) {
		getterCalled();
		return wrappedList.contains(o);
	}

	@Override
	public boolean containsAll(Collection c) {
		getterCalled();
		return wrappedList.containsAll(c);
	}

	@Override
	public boolean equals(Object o) {
		getterCalled();
		return o == this || wrappedList.equals(o);
	}

	@Override
	public int hashCode() {
		getterCalled();
		return wrappedList.hashCode();
	}

	@Override
	public boolean isEmpty() {
		getterCalled();
		return wrappedList.isEmpty();
	}

	@Override
	public Iterator iterator() {
		getterCalled();
		final Iterator wrappedIterator = wrappedList.iterator();
		return new Iterator() {

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return wrappedIterator.hasNext();
			}

			@Override
			public Object next() {
				return wrappedIterator.next();
			}
		};
	}

	@Override
	public int size() {
		getterCalled();
		return wrappedList.size();
	}

	@Override
	public Object[] toArray() {
		getterCalled();
		return wrappedList.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		getterCalled();
		return wrappedList.toArray(a);
	}

	@Override
	public String toString() {
		getterCalled();
		return wrappedList.toString();
	}

	/**
	 * @TrackedGetter
	 */
	@Override
	public Object get(int index) {
		getterCalled();
		return wrappedList.get(index);
	}

	/**
	 * @TrackedGetter
	 */
	@Override
	public int indexOf(Object o) {
		getterCalled();
		return wrappedList.indexOf(o);
	}

	/**
	 * @TrackedGetter
	 */
	@Override
	public int lastIndexOf(Object o) {
		getterCalled();
		return wrappedList.lastIndexOf(o);
	}

	// List Iterators

	/**
	 * @TrackedGetter
	 */
	@Override
	public ListIterator listIterator() {
		return listIterator(0);
	}

	/**
	 * @TrackedGetter
	 */
	@Override
	public ListIterator listIterator(int index) {
		getterCalled();
		final ListIterator wrappedIterator = wrappedList.listIterator(index);
		return new ListIterator() {

			@Override
			public int nextIndex() {
				return wrappedIterator.nextIndex();
			}

			@Override
			public int previousIndex() {
				return wrappedIterator.previousIndex();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return wrappedIterator.hasNext();
			}

			@Override
			public boolean hasPrevious() {
				return wrappedIterator.hasPrevious();
			}

			@Override
			public Object next() {
				return wrappedIterator.next();
			}

			@Override
			public Object previous() {
				return wrappedIterator.previous();
			}

			@Override
			public void add(Object o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(Object o) {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public List subList(final int fromIndex, final int toIndex) {
		getterCalled();
		if (fromIndex < 0 || fromIndex > toIndex || toIndex > size()) {
			throw new IndexOutOfBoundsException();
		}
		return new AbstractObservableList(getRealm()) {

			@Override
			public Object getElementType() {
				return ObservableList.this.getElementType();
			}

			@Override
			public Object get(int location) {
				return ObservableList.this.get(fromIndex + location);
			}

			@Override
			protected int doGetSize() {
				return toIndex - fromIndex;
			}
		};
	}

	protected void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	@Override
	public Object set(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Moves the element located at <code>oldIndex</code> to
	 * <code>newIndex</code>. This method is equivalent to calling
	 * <code>add(newIndex, remove(oldIndex))</code>.
	 * <p>
	 * Subclasses should override this method to deliver list change
	 * notification for the remove and add operations in the same
	 * ListChangeEvent, as this allows {@link ListDiff#accept(ListDiffVisitor)}
	 * to recognize the operation as a move.
	 *
	 * @param oldIndex
	 *            the element's position before the move. Must be within the
	 *            range <code>0 &lt;= oldIndex &lt; size()</code>.
	 * @param newIndex
	 *            the element's position after the move. Must be within the
	 *            range <code>0 &lt;= newIndex &lt; size()</code>.
	 * @return the element that was moved.
	 * @throws IndexOutOfBoundsException
	 *             if either argument is out of range (
	 *             <code>0 &lt;= index &lt; size()</code>).
	 * @see ListDiffVisitor#handleMove(int, int, Object)
	 * @see ListDiff#accept(ListDiffVisitor)
	 * @since 1.1
	 */
	@Override
	public Object move(int oldIndex, int newIndex) {
		checkRealm();
		int size = wrappedList.size();
		if (oldIndex < 0 || oldIndex >= size)
			throw new IndexOutOfBoundsException(
					"oldIndex: " + oldIndex + ", size:" + size); //$NON-NLS-1$ //$NON-NLS-2$
		if (newIndex < 0 || newIndex >= size)
			throw new IndexOutOfBoundsException(
					"newIndex: " + newIndex + ", size:" + size); //$NON-NLS-1$ //$NON-NLS-2$
		Object element = remove(oldIndex);
		add(newIndex, element);
		return element;
	}

	@Override
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the stale state. Must be invoked from the current realm.
	 *
	 * @return stale state
	 */
	@Override
	public boolean isStale() {
		getterCalled();
		return stale;
	}

	/**
	 * Sets the stale state. Must be invoked from the current realm.
	 *
	 * @param stale
	 *            The stale state to list. This will fire a stale event if the
	 *            given boolean is true and this observable list was not already
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

	@Override
	protected void fireChange() {
		throw new RuntimeException(
				"fireChange should not be called, use fireListChange() instead"); //$NON-NLS-1$
	}

	@Override
	public synchronized void dispose() {
		super.dispose();
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	protected void updateWrappedList(List newList) {
		List oldList = wrappedList;
		ListDiff listDiff = Diffs.computeListDiff(oldList, newList);
		wrappedList = newList;
		fireListChange(listDiff);
	}

}
