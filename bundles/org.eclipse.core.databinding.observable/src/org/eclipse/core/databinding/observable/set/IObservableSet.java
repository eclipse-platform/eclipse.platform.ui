/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 237718
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservableCollection;

/**
 * A set whose changes can be tracked by set change listeners.
 *
 * @param <E>
 *            the type of the elements in this set
 *
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the classes that
 *              implement this interface. Note that direct implementers of this
 *              interface outside of the framework will be broken in future
 *              releases when methods are added to this interface.
 *
 * @see AbstractObservableSet
 * @see ObservableSet
 *
 * @since 1.0
 *
 */
public interface IObservableSet<E> extends Set<E>, IObservableCollection<E> {

	/**
	 * @param listener
	 */
	public void addSetChangeListener(ISetChangeListener<? super E> listener);

	/**
	 * @param listener
	 */
	public void removeSetChangeListener(ISetChangeListener<? super E> listener);

	/**
	 * @return the element type or <code>null</code> if untyped
	 */
	@Override
	public Object getElementType();

	/**
	 * @TrackedGetter
	 */
	@Override
	int size();

	/**
	 * @TrackedGetter
	 */
	@Override
	boolean isEmpty();

	/**
	 * @TrackedGetter
	 */
	@Override
	boolean contains(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override
	Iterator<E> iterator();

	/**
	 * @TrackedGetter
	 */
	@Override
	Object[] toArray();

	/**
	 * @TrackedGetter
	 */
	@Override
	<T> T[] toArray(T a[]);

	// Modification Operations

	/**
	 * @TrackedGetter
	 */
	@Override
	boolean add(E o);

	/**
	 * @TrackedGetter
	 */
	@Override
	boolean remove(Object o);

	// Bulk Operations

	/**
	 * @TrackedGetter
	 */
	@Override
	boolean containsAll(Collection<?> c);

	/**
	 * @TrackedGetter
	 */
	@Override
	boolean addAll(Collection<? extends E> c);

	/**
	 * @TrackedGetter
	 */
	@Override
	boolean retainAll(Collection<?> c);

	/**
	 * @TrackedGetter
	 */
	@Override
	boolean removeAll(Collection<?> c);

	// Comparison and hashing

	/**
	 * @TrackedGetter
	 */
	@Override
	boolean equals(Object o);

	/**
	 * @TrackedGetter
	 */
	@Override
	int hashCode();

}
