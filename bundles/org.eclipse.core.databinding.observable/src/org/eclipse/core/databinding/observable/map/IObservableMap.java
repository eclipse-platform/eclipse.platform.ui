/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Matthew Hall - bug 237718, 226289
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.IObservable;

/**
 * Observable Map.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 *              Clients should instead subclass one of the classes that
 *              implement this interface. Note that direct implementers of this
 *              interface outside of the framework will be broken in future
 *              releases when methods are added to this interface.
 *              
 * @see AbstractObservableMap
 * @see ObservableMap
 * 
 * @since 1.1
 */
public interface IObservableMap extends Map, IObservable {
	
	/**
	 * Returns the element type for the {@link #keySet() keyset} of this
	 * observable map, or <code>null</code> if the keyset is untyped.
	 * 
	 * @return the element type for the {@link #keySet() keyset} of this
	 *         observable map, or <code>null</code> if the keyset is untyped.
	 * @since 1.2
	 */
	public Object getKeyType();

	/**
	 * Returns the element type for the {@link #values() values} of this
	 * observable map, or <code>null</code> if the values collection is untyped.
	 * 
	 * @return the element type for the {@link #values() values} of this
	 *         observable map, or <code>null</code> if the values collection is
	 *         untyped.
	 * @since 1.2
	 */
	public Object getValueType();

	/**
	 * @param listener
	 */
	public void addMapChangeListener(IMapChangeListener listener);

	/**
	 * @param listener
	 */
	public void removeMapChangeListener(IMapChangeListener listener);
	
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
	public boolean containsKey(Object key);

	/**
	 * @TrackedGetter
	 */
	public boolean containsValue(Object value);

	/**
	 * @TrackedGetter
	 */
	public Object get(Object key);

	/**
	 * 
	 */
	public Object put(Object key, Object value);

	/**
	 * 
	 */
	public Object remove(Object key);

	/**
	 * @TrackedGetter
	 */
	public Set keySet();

	/**
	 * @TrackedGetter
	 */
	public Collection values();

	/**
	 * @TrackedGetter
	 */
	public Set entrySet();

	/**
	 * @TrackedGetter
	 */
	public boolean equals(Object o);

	/**
	 * @TrackedGetter
	 */
	public int hashCode();
}
