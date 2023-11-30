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
 *              implement this interface.
 *              <p>
 *              Authors of extensions to the databinding framework may extend
 *              this interface and indirectly implement it, but if doing so must
 *              also extend one of the framework classes. (Use an API problem
 *              filter to suppress the resulting warning.)
 *              <p>
 *              Direct implementers of this interface outside of the framework
 *              will be broken in future releases when methods are added to this
 *              interface.
 *
 * @see AbstractObservableSet
 * @see ObservableSet
 *
 * @since 1.0
 */
public interface IObservableSet<E> extends Set<E>, IObservableCollection<E> {

	/**
	 * @param listener the change listener to add; not <code>null</code>
	 */
	public void addSetChangeListener(ISetChangeListener<? super E> listener);

	/**
	 * @param listener the change listener to remove; not <code>null</code>
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
