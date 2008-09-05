/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 237718
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservableCollection;

/**
 * A set whose changes can be tracked by set change listeners.
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
	 * @return the element type or <code>null</code> if untyped
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
