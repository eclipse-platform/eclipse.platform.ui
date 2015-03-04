/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.*;
import org.eclipse.core.internal.utils.IStringPoolParticipant;
import org.eclipse.core.internal.utils.StringPool;

/**
 * A specialized map implementation that is optimized for a 
 * small set of interned strings as keys.  The provided keys
 * MUST be instances of java.lang.String.
 * 
 * Implemented as a single array that alternates keys and values.
 */
@SuppressWarnings("unchecked")
public class MarkerAttributeMap<V> implements Map<String, V>, IStringPoolParticipant {
	protected Object[] elements = null;
	protected int count = 0;

	// 8 attribute keys, 8 attribute values
	protected static final int DEFAULT_SIZE = 16;
	protected static final int GROW_SIZE = 10;

	private static final Object[] EMPTY = new Object[0];

	/**
	 * Creates a new marker attribute map of default size
	 */
	public MarkerAttributeMap() {
		this(DEFAULT_SIZE);
	}

	/**
	 * Creates a new marker attribute map.
	 * @param initialCapacity The initial number of elements that will fit in the map.
	 */
	public MarkerAttributeMap(int initialCapacity) {
		elements = initialCapacity > 0 ? new Object[initialCapacity * 2] : EMPTY;
	}

	/**
	 * Creates a new marker attribute map of default size
	 * @param map The entries in the given map will be added to the new map.
	 */
	public MarkerAttributeMap(Map<String, ? extends V> map) {
		this(map.size());
		putAll(map);
	}

	/* (non-Javadoc)
	 * @see Map#clear()
	 */
	@Override
	public void clear() {
		count = 0;
		elements = EMPTY;
	}

	/* (non-Javadoc)
	 * @see Map#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey(Object key) {
		if (count == 0)
			return false;
		key = ((String) key).intern();
		for (int i = 0; i < elements.length; i = i + 2)
			if (elements[i] == key)
				return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see Map#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		if (count == 0)
			return false;
		for (int i = 1; i < elements.length; i = i + 2)
			if (elements[i] != null && elements[i].equals(value))
				return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see Map#entrySet()
	 * This implementation does not conform properly to the specification
	 * in the Map interface.  The returned collection will not be bound to
	 * this map and will not remain in sync with this map.
	 */
	@Override
	public Set<Entry<String, V>> entrySet() {
		return toHashMap().entrySet();
	}

	/* (non-Javadoc)
	 * @see Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Map))
			return false;
		Map<String, V> other = (Map<String, V>) o;
		//must be same size
		if (count != other.size())
			return false;

		if (count == 0)
			return true;

		//keysets must be equal
		if (!keySet().equals(other.keySet()))
			return false;

		//values for each key must be equal
		for (int i = 0; i < elements.length; i = i + 2) {
			if (elements[i] != null && (!elements[i + 1].equals(other.get(elements[i]))))
				return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see Map#get(java.lang.Object)
	 */
	@Override
	public V get(Object key) {
		if (count == 0)
			return null;
		key = ((String) key).intern();
		for (int i = 0; i < elements.length; i = i + 2)
			if (elements[i] == key)
				return (V) elements[i + 1];
		return null;
	}

	/**
	 * The capacity of the map has been exceeded, grow the array by
	 * GROW_SIZE to accomodate more entries.
	 */
	protected void grow() {
		Object[] expanded = new Object[elements.length + GROW_SIZE];
		System.arraycopy(elements, 0, expanded, 0, elements.length);
		elements = expanded;
	}

	/* (non-Javadoc)
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 0;
		if (count == 0)
			return hash;
		for (int i = 0; i < elements.length; i = i + 2) {
			if (elements[i] != null) {
				hash += elements[i].hashCode();
			}
		}
		return hash;
	}

	/* (non-Javadoc)
	 * @see Map#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return count == 0;
	}

	/* (non-Javadoc)
	 * @see Map#keySet()
	 * This implementation does not conform properly to the specification
	 * in the Map interface.  The returned collection will not be bound to
	 * this map and will not remain in sync with this map.
	 */
	@Override
	public Set<String> keySet() {
		Set<String> result = new HashSet<String>(size());
		if (count == 0)
			return result;
		for (int i = 0; i < elements.length; i = i + 2) {
			if (elements[i] != null) {
				result.add((String) elements[i]);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public V put(String k, V value) {
		if (k == null)
			throw new NullPointerException();
		if (value == null)
			return remove(k);
		String key = k.intern();

		if (elements.length <= (count * 2))
			grow();

		// handle the case where we don't have any attributes yet
		if (count == 0) {
			elements[0] = key;
			elements[1] = value;
			count++;
			return null;
		}

		// replace existing value if it exists
		for (int i = 0; i < elements.length; i = i + 2) {
			if (elements[i] == key) {
				Object oldValue = elements[i + 1];
				elements[i + 1] = value;
				return (V) oldValue;
			}
		}

		// otherwise add it to the list of elements.
		for (int i = 0; i < elements.length; i = i + 2) {
			if (elements[i] == null) {
				elements[i] = key;
				elements[i + 1] = value;
				count++;
				return null;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see Map#putAll(java.util.Map)
	 */
	@Override
	public void putAll(Map<? extends String, ? extends V> map) {
		for (Map.Entry<? extends String, ? extends V> e : map.entrySet())
			put(e.getKey(), e.getValue());
	}

	/* (non-Javadoc)
	 * @see Map#remove(java.lang.Object)
	 */
	@Override
	public V remove(Object key) {
		if (count == 0)
			return null;
		key = ((String) key).intern();
		for (int i = 0; i < elements.length; i = i + 2) {
			if (elements[i] == key) {
				elements[i] = null;
				Object result = elements[i + 1];
				elements[i + 1] = null;
				count--;
				return (V) result;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see Map#size()
	 */
	@Override
	public int size() {
		return count;
	}

	/* (non-Javadoc
	 * Method declared on IStringPoolParticipant
	 */
	@Override
	public void shareStrings(StringPool set) {
		//copy elements for thread safety
		Object[] array = elements;
		if (array == null)
			return;
		//don't share keys because they are already interned
		for (int i = 1; i < array.length; i = i + 2) {
			Object o = array[i];
			if (o instanceof String)
				array[i] = set.add((String) o);
			else if (o instanceof IStringPoolParticipant)
				((IStringPoolParticipant) o).shareStrings(set);
		}
	}

	/**
	 * Creates a new hash map with the same contents as this map.
	 */
	private HashMap<String, V> toHashMap() {
		HashMap<String, V> result = new HashMap<String, V>(size());
		if (count == 0)
			return result;
		for (int i = 0; i < elements.length; i = i + 2) {
			if (elements[i] != null) {
				result.put((String) elements[i], (V) elements[i + 1]);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see Map#values()
	 * This implementation does not conform properly to the specification
	 * in the Map interface.  The returned collection will not be bound to
	 * this map and will not remain in sync with this map.
	 */
	@Override
	public Collection<V> values() {
		Set<V> result = new HashSet<V>(size());
		if (count == 0)
			return result;
		for (int i = 1; i < elements.length; i = i + 2) {
			if (elements[i] != null) {
				result.add((V) elements[i]);
			}
		}
		return result;
	}
}
