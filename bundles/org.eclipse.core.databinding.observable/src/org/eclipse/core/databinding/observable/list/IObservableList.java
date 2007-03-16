/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 167204
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
 * <p>
 * This interface is not intended to be implemented by clients. Clients should
 * instead subclass one of the framework classes that implement this interface.
 * Note that direct implementers of this interface outside of the framework will
 * be broken in future releases when methods are added to this interface.
 * </p>
 * 
 * @since 1.0
 */
public interface IObservableList extends List, IObservableCollection {
	
	/**
	 * Adds the given list change listener to the list of list change listeners.
	 * @param listener
	 */
	public void addListChangeListener(IListChangeListener listener);
	
	/**
	 * Removes the given list change listener from the list of list change listeners.
	 * Has no effect if the given listener is not registered as a list change listener.
	 * 
	 * @param listener
	 */
	public void removeListChangeListener(IListChangeListener listener);

	/**
	 * @TrackedGetter
	 */
    public int size();

	/**
	 * @TrackedGetter
	 */
    public boolean isEmpty();

	/**
	 * @TrackedGetter
	 */
    public boolean contains(Object o);

	/**
	 * @TrackedGetter
	 */
    public Iterator iterator();

	/**
	 * @TrackedGetter
	 */
    public Object[] toArray();

	/**
	 * @TrackedGetter
	 */
    public Object[] toArray(Object a[]);

	/**
	 * 
	 */
    public boolean add(Object o);

	/**
	 * 
	 */
    public boolean remove(Object o);

	/**
	 * @TrackedGetter
	 */
    public boolean containsAll(Collection c);

	/**
	 * 
	 */
    public boolean addAll(Collection c);

	/**
	 * 
	 */
    public boolean addAll(int index, Collection c);

	/**
	 * 
	 */
    public boolean removeAll(Collection c);

	/**
	 *
	 */
    public boolean retainAll(Collection c);

	/**
	 * @TrackedGetter
	 */
    public boolean equals(Object o);

	/**
	 * @TrackedGetter
	 */
    public int hashCode();

	/**
	 * @TrackedGetter
	 */
    public Object get(int index);

	/**
	 * 
	 */
    public Object set(int index, Object element);

	/**
	 * 
	 */
    public Object remove(int index);

	/**
	 * @TrackedGetter
	 */
    public int indexOf(Object o);

	/**
	 * @TrackedGetter
	 */
    public int lastIndexOf(Object o);

	/**
	 * @TrackedGetter
	 */
    public ListIterator listIterator();

	/**
	 * @TrackedGetter
	 */
    public ListIterator listIterator(int index);

	/**
	 * @TrackedGetter
	 */
    public List subList(int fromIndex, int toIndex);

	/**
	 * @return the type of the elements or <code>null</code> if untyped
	 */
	Object getElementType();
}
