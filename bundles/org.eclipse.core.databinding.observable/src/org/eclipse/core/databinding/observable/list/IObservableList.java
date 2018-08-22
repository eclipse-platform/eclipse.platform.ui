/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 167204
 *     Matthew Hall - bugs 208858, 237718
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.IObservableCollection;

/**
 * A list whose changes can be tracked by list change listeners.
 *
 * @param <E>
 *            the type of elements in this collection
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the framework classes
 *              that implement this interface. Note that direct implementers of
 *              this interface outside of the framework will be broken in future
 *              releases when methods are added to this interface.
 *
 * @see AbstractObservableList
 * @see ObservableList
 *
 * @since 1.0
 */
public interface IObservableList<E> extends List<E>, IObservableCollection<E> {

	/**
	 * Adds the given list change listener to the list of list change listeners.
	 *
	 * @param listener
	 */
	void addListChangeListener(IListChangeListener<? super E> listener);

	/**
	 * Removes the given list change listener from the list of list change
	 * listeners. Has no effect if the given listener is not registered as a
	 * list change listener.
	 *
	 * @param listener
	 */
	void removeListChangeListener(IListChangeListener<? super E> listener);

	/**
	 * @TrackedGetter
	 */
	@Override int size();

	/**
	 * @TrackedGetter
	 */
	@Override boolean isEmpty();

	/**
	 * @TrackedGetter
	 */
	@Override boolean contains(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override Iterator<E> iterator();

	/**
	 * @TrackedGetter
	 */
	@Override Object[] toArray();

	/**
	 * @TrackedGetter
	 */
	@Override <T> T[] toArray(T a[]);

	/**
	 *
	 */
	@Override boolean add(E o);

	/**
	 *
	 */
	@Override boolean remove(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override boolean containsAll(Collection<?> c);

	/**
	 *
	 */
	@Override boolean addAll(Collection<? extends E> c);

	/**
	 *
	 */
	@Override boolean addAll(int index, Collection<? extends E> c);

	/**
	 *
	 */
	@Override boolean removeAll(Collection<?> c);

	/**
	 *
	 */
	@Override boolean retainAll(Collection<?> c);

	/**
	 * @TrackedGetter
	 */
	@Override boolean equals(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override int hashCode();

	/**
	 * @TrackedGetter
	 */
	@Override E get(int index);

	/**
	 *
	 */
	@Override E set(int index, E element);

	/**
	 * Moves the element located at <code>oldIndex</code> to
	 * <code>newIndex</code>. This method is equivalent to calling
	 * <code>add(newIndex, remove(oldIndex))</code>.
	 * <p>
	 * Implementors should deliver list change notification for the remove and
	 * add operations in the same ListChangeEvent, as this allows
	 * {@link ListDiff#accept(ListDiffVisitor)} to recognize the operation as a
	 * move.
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
	E move(int oldIndex, int newIndex);

	/**
	 *
	 */
	@Override E remove(int index);

	/**
	 * @TrackedGetter
	 */
	@Override int indexOf(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override int lastIndexOf(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override ListIterator<E> listIterator();

	/**
	 * @TrackedGetter
	 */
	@Override ListIterator<E> listIterator(int index);

	/**
	 * @TrackedGetter
	 */
	@Override List<E> subList(int fromIndex, int toIndex);

	/**
	 * @return the type of the elements or <code>null</code> if untyped
	 */
	@Override
	Object getElementType();
}
