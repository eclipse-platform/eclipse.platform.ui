/**
 *  Copyright 2001-2004 The Apache Software Foundation
 *  Portions (modifications) Copyright 2004-2005 IBM Corp.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * Contributors:
 *    Apache Software Foundation - Initial implementation
 *    Pascal Rapicault, IBM -  Pascal remove the entrySet() implementation because it relied on another class.
 *    IBM - change to int keys, remove support for weak references, and remove unused methods
 */
package org.eclipse.core.internal.registry;

import java.lang.ref.*;

/**
 *  Hashtable-based map with integer keys that allows values to be removed 
 *  by the garbage  collector.<P>
 *
 *  When you construct a <Code>ReferenceMap</Code>, you can 
 *  specify what kind of references are used to store the
 *  map's values.  If non-hard references are 
 *  used, then the garbage collector can remove mappings
 *  if a value becomes unreachable, or if the 
 *  JVM's memory is running low.  For information on how
 *  the different reference types behave, see
 *  {@link Reference}.<P>
 *
 *  The algorithms used are basically the same as those
 *  in {@link java.util.HashMap}.  In particular, you 
 *  can specify a load factor and capacity to suit your
 *  needs.
 *
 *  This map does <I>not</I> allow null values.  Attempting to add a null 
 *  value to the map will raise a <Code>NullPointerException</Code>.<P>
 *
 *  This data structure is not synchronized.
 *
 *  @see java.lang.ref.Reference
 */
public class ReferenceMap {

	/**
	 * IEntry implementation that acts as a hard reference.
	 * The value of a hard reference entry is never garbage
	 * collected until it is explicitly removed from the map.
	 */
	private static class HardRef implements IEntry {

		private int key;
		private IEntry next;
		/**
		 * Reference value.  Note this can never be null.
		 */
		private Object value;

		public HardRef(int key, Object value, IEntry next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}

		public int getKey() {
			return key;
		}

		public IEntry getNext() {
			return next;
		}

		public Object getValue() {
			return value;
		}

		public void setNext(IEntry next) {
			this.next = next;
		}

		public String toString() {
			return "HardRef(" + key + ',' + value + ')'; //$NON-NLS-1$
		}
	}

	/**
	 * The common interface for all elements in the map.  Both
	 * hard and soft map values conform to this interface.
	 */
	private static interface IEntry {
		/**
		 * Returns the integer key for this entry.
		 * @return The integer key
		 */
		public int getKey();

		/**
		 * Returns the next entry in the linked list of entries
		 * with the same hash value, or <code>null</code>
		 * if there is no next entry.
		 * @return The next entry, or <code>null</code>.
		 */
		public IEntry getNext();

		/**
		 * Returns the value of this entry.
		 * @return The entry value.
		 */
		public Object getValue();

		/**
		 * Sets the next entry in the linked list of map entries
		 * with the same hash value.
		 * 
		 * @param next The next entry, or <code>null</code>.
		 */
		public void setNext(IEntry next);
	}

	/**
	 * Augments a normal soft reference with additional information
	 * required to implement the IEntry interface.
	 */
	private static class SoftRef extends SoftReference implements IEntry {
		private int key;
		/**
		 * For chained collisions
		 */
		private IEntry next;

		public SoftRef(int key, Object value, IEntry next, ReferenceQueue q) {
			super(value, q);
			this.key = key;
			this.next = next;
		}

		public int getKey() {
			return key;
		}

		public IEntry getNext() {
			return next;
		}

		public Object getValue() {
			return super.get();
		}

		public void setNext(IEntry next) {
			this.next = next;
		}
	}

	/**
	 *  Constant indicating that hard references should be used.
	 */
	final public static int HARD = 0;

	/**
	 *  Constant indiciating that soft references should be used.
	 */
	final public static int SOFT = 1;

	private int entryCount;

	/**
	 *  The threshold variable is calculated by multiplying
	 *  table.length and loadFactor.  
	 *  Note: I originally marked this field as final, but then this class
	 *   didn't compile under JDK1.2.2.
	 *  @serial
	 */
	private float loadFactor;

	/**
	 *  ReferenceQueue used to eliminate stale mappings.
	 */
	private transient ReferenceQueue queue = new ReferenceQueue();

	/**
	 *  Number of mappings in this map.
	 */
	private transient int size;

	/**
	 *  The hash table.  Its length is always a power of two.  
	 */
	private transient IEntry[] table;

	/**
	 *  When size reaches threshold, the map is resized.  
	 *  @see #resize()
	 */
	private transient int threshold;

	/**
	 *  The reference type for values.  Must be HARD or SOFT
	 *  Note: I originally marked this field as final, but then this class
	 *   didn't compile under JDK1.2.2.
	 *  @serial
	 */
	int valueType;

	/**
	 *  Constructs a new <Code>ReferenceMap</Code> with the
	 *  specified reference type, load factor and initial
	 *  capacity.
	 *
	 *  @param referenceType  the type of reference to use for values;
	 *   must be {@link #HARD} or {@link #SOFT}
	 *  @param capacity  the initial capacity for the map
	 *  @param loadFactor  the load factor for the map
	 */
	public ReferenceMap(int referenceType, int capacity, float loadFactor) {
		super();
		if (referenceType != HARD && referenceType != SOFT)
			throw new IllegalArgumentException(" must be HARD or SOFT."); //$NON-NLS-1$
		if (capacity <= 0)
			throw new IllegalArgumentException("capacity must be positive"); //$NON-NLS-1$
		if ((loadFactor <= 0.0f) || (loadFactor >= 1.0f))
			throw new IllegalArgumentException("Load factor must be greater than 0 and less than 1."); //$NON-NLS-1$

		this.valueType = referenceType;

		int initialSize = 1;
		while (initialSize < capacity)
			initialSize *= 2;

		this.table = new IEntry[initialSize];
		this.loadFactor = loadFactor;
		this.threshold = (int) (initialSize * loadFactor);
	}

	/**
	 * @param key
	 * @return
	 */
	private Object doRemove(int key) {
		int index = indexFor(key);
		IEntry previous = null;
		IEntry entry = table[index];
		while (entry != null) {
			if (key == entry.getKey()) {
				if (previous == null)
					table[index] = entry.getNext();
				else
					previous.setNext(entry.getNext());
				this.size--;
				return entry.getValue();
			}
			previous = entry;
			entry = entry.getNext();
		}
		return null;
	}

	/**
	 *  Returns the value associated with the given key, if any.
	 *
	 *  @return the value associated with the given key, or <Code>null</Code>
	 *   if the key maps to no value
	 */
	public Object get(int key) {
		purge();
		for (IEntry entry = table[indexFor(key)]; entry != null; entry = entry.getNext())
			if (entry.getKey() == key)
				return entry.getValue();
		return null;
	}

	/**
	 *  Converts the given hash code into an index into the
	 *  hash table.
	 */
	private int indexFor(int hash) {
		// mix the bits to avoid bucket collisions...
		hash += ~(hash << 15);
		hash ^= (hash >>> 10);
		hash += (hash << 3);
		hash ^= (hash >>> 6);
		hash += ~(hash << 11);
		hash ^= (hash >>> 16);
		return hash & (table.length - 1);
	}

	/**
	 * Constructs a new table entry for the given data
	 * 
	 * @param key The entry key
	 * @param value The entry value
	 * @param next The next value in the entry's collision chain
	 * @return The new table entry
	 */
	private IEntry newEntry(int key, Object value, IEntry next) {
		entryCount++;
		switch (valueType) {
			case HARD :
				return new HardRef(key, value, next);
			case SOFT :
				return new SoftRef(key, value, next, queue);
			default :
				throw new Error();
		}
	}

	/**
	 *  Purges stale mappings from this map.<P>
	 *
	 *  Ordinarily, stale mappings are only removed during
	 *  a write operation; typically a write operation will    
	 *  occur often enough that you'll never need to manually
	 *  invoke this method.<P>
	 *
	 *  Note that this method is not synchronized!  Special
	 *  care must be taken if, for instance, you want stale
	 *  mappings to be removed on a periodic basis by some
	 *  background thread.
	 */
	private void purge() {
		Reference ref = queue.poll();
		while (ref != null) {
			doRemove(((IEntry) ref).getKey());
			ref.clear();
			ref = queue.poll();
		}
	}

	/**
	 *  Associates the given key with the given value.<P>
	 *  Neither the key nor the value may be null.
	 *
	 *  @param key  the key of the mapping
	 *  @param value  the value of the mapping
	 *  @throws NullPointerException if either the key or value
	 *   is null
	 */
	public void put(int key, Object value) {
		if (value == null)
			throw new NullPointerException("null values not allowed"); //$NON-NLS-1$

		purge();

		if (size + 1 > threshold)
			resize();

		int index = indexFor(key);
		IEntry previous = null;
		IEntry entry = table[index];
		while (entry != null) {
			if (key == entry.getKey()) {
				if (previous == null)
					table[index] = newEntry(key, value, entry.getNext());
				else
					previous.setNext(newEntry(key, value, entry.getNext()));
				return;
			}
			previous = entry;
			entry = entry.getNext();
		}
		this.size++;
		table[index] = newEntry(key, value, table[index]);
	}

	/**
	 *  Removes the key and its associated value from this map.
	 *
	 *  @param key  the key to remove
	 *  @return the value associated with that key, or null if
	 *   the key was not in the map
	 */
	public Object remove(int key) {
		purge();
		return doRemove(key);
	}

	/**
	 *  Resizes this hash table by doubling its capacity.
	 *  This is an expensive operation, as entries must
	 *  be copied from the old smaller table to the new 
	 *  bigger table.
	 */
	private void resize() {
		IEntry[] old = table;
		table = new IEntry[old.length * 2];

		for (int i = 0; i < old.length; i++) {
			IEntry next = old[i];
			while (next != null) {
				IEntry entry = next;
				next = next.getNext();
				int index = indexFor(entry.getKey());
				entry.setNext(table[index]);
				table[index] = entry;
			}
			old[i] = null;
		}
		threshold = (int) (table.length * loadFactor);
	}
}