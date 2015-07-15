/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public void addListChangeListener(IListChangeListener<? super E> listener);

	/**
	 * Removes the given list change listener from the list of list change
	 * listeners. Has no effect if the given listener is not registered as a
	 * list change listener.
	 *
	 * @param listener
	 */
	public void removeListChangeListener(IListChangeListener<? super E> listener);

	/**
	 * @TrackedGetter
	 */
	@Override
	public int size();

	/**
	 * @TrackedGetter
	 */
	@Override
	public boolean isEmpty();

	/**
	 * @TrackedGetter
	 */
	@Override
	public boolean contains(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override
	public Iterator<E> iterator();

	/**
	 * @TrackedGetter
	 */
	@Override
	public Object[] toArray();

	/**
	 * @TrackedGetter
	 */
	@Override
	public <T> T[] toArray(T a[]);

	/**
	 *
	 */
	@Override
	public boolean add(E o);

	/**
	 *
	 */
	@Override
	public boolean remove(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override
	public boolean containsAll(Collection<?> c);

	/**
	 *
	 */
	@Override
	public boolean addAll(Collection<? extends E> c);

	/**
	 *
	 */
	@Override
	public boolean addAll(int index, Collection<? extends E> c);

	/**
	 *
	 */
	@Override
	public boolean removeAll(Collection<?> c);

	/**
	 *
	 */
	@Override
	public boolean retainAll(Collection<?> c);

	/**
	 * @TrackedGetter
	 */
	@Override
	public boolean equals(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override
	public int hashCode();

	/**
	 * @TrackedGetter
	 */
	@Override
	public E get(int index);

	/**
	 *
	 */
	@Override
	public E set(int index, E element);

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
	public E move(int oldIndex, int newIndex);

	/**
	 *
	 */
	@Override
	public E remove(int index);

	/**
	 * @TrackedGetter
	 */
	@Override
	public int indexOf(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override
	public int lastIndexOf(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override
	public ListIterator<E> listIterator();

	/**
	 * @TrackedGetter
	 */
	@Override
	public ListIterator<E> listIterator(int index);

	/**
	 * @TrackedGetter
	 */
	@Override
	public List<E> subList(int fromIndex, int toIndex);

	/**
	 * @return the type of the elements or <code>null</code> if untyped
	 */
	@Override
	Object getElementType();
}
