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

package org.eclipse.jface.internal.databinding.provisional.observable.set;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.internal.databinding.provisional.observable.IObservableCollection;

/**
 * 
 * Clients must subclass {@link ObservableSet} instead of implementing this
 * interface directly.
 * 
 * @since 1.0
 * 
 */
public interface IObservableSet extends Set, IObservableCollection {

	/**
	 * @param listener
	 */
	public void addSetChangeListener(ISetChangeListener listener);

	/**
	 * @param listener
	 */
	public void removeSetChangeListener(ISetChangeListener listener);

	/**
	 * @return the element type
	 */
	public Object getElementType();

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
	 * @TrackedGetter
	 */
	boolean add(Object o);

	/**
	 * @TrackedGetter
	 */
	boolean remove(Object o);

	// Bulk Operations

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
	boolean retainAll(Collection c);

	/**
	 * @TrackedGetter
	 */
	boolean removeAll(Collection c);

	// Comparison and hashing

	/**
	 * @TrackedGetter
	 */
	boolean equals(Object o);

	/**
	 * @TrackedGetter
	 */
	int hashCode();

}
