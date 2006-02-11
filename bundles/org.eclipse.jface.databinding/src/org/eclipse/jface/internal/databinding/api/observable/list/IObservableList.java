/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.api.observable.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.jface.internal.databinding.api.observable.IObservable;

/**
 * @since 3.2
 *
 */
public interface IObservableList extends List, IObservable {
	
	public void addListChangeListener(IListChangeListener listener);
	
	public void removeListChangeListener(IListChangeListener listener);

    // Query Operations

	/**
	 * @TrackedGetter
	 */
    int size();

	/**
	 * @TrackedGetter
	 */
    boolean isEmpty();

	/**
	 * @TrackedGetter
	 */
    boolean contains(Object o);

	/**
	 * @TrackedGetter
	 */
    Iterator iterator();

	/**
	 * @TrackedGetter
	 */
    Object[] toArray();

	/**
	 * @TrackedGetter
	 */
    Object[] toArray(Object a[]);


    // Modification Operations

	/**
	 * @TrackedGetter because of the returned boolean
	 */
    boolean add(Object o);

	/**
	 * @TrackedGetter
	 */
    boolean remove(Object o);


    // Bulk Modification Operations

	/**
	 * @TrackedGetter
	 */
    boolean containsAll(Collection c);

	/**
	 * @TrackedGetter
	 */
    boolean addAll(Collection c);

	/**
	 * @TrackedGetter
	 */
    boolean addAll(int index, Collection c);

	/**
	 * @TrackedGetter
	 */
    boolean removeAll(Collection c);

	/**
	 * @TrackedGetter
	 */
    boolean retainAll(Collection c);

    // Comparison and hashing

	/**
	 * @TrackedGetter
	 */
    boolean equals(Object o);

	/**
	 * @TrackedGetter
	 */
    int hashCode();


    // Positional Access Operations

	/**
	 * @TrackedGetter
	 */
    Object get(int index);

	/**
	 * @TrackedGetter because of the returned object
	 */
    Object set(int index, Object element);

	/**
	 * @TrackedGetter
	 */
    Object remove(int index);


    // Search Operations

	/**
	 * @TrackedGetter
	 */
    int indexOf(Object o);

	/**
	 * @TrackedGetter
	 */
    int lastIndexOf(Object o);


    // List Iterators

	/**
	 * @TrackedGetter
	 */
    ListIterator listIterator();

	/**
	 * @TrackedGetter
	 */
    ListIterator listIterator(int index);

    // View

	/**
	 * @TrackedGetter
	 */
    List subList(int fromIndex, int toIndex);
}
