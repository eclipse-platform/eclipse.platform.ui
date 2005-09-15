/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import org.eclipse.core.internal.preferences.StringPool;

/**
 * Hashtable of {String --> String}.
 * 
 * This map handles collisions using linear probing.  When elements are
 * removed, the entire table is rehashed.  Thus this map has good space
 * characteristics, good insertion and iteration performance, but slower
 * removal performance than a HashMap.
 * <p>
 * This map is not thread safe.
 */
public final class HashMapOfString {
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	/**
	 * number of elements in the table
	 */
	private int elementSize;
	/**
	 * The table keys
	 */
	private String[] keyTable;

	private int threshold;

	private String[] valueTable;
	private static final float LOAD_FACTOR = 0.45f;

	public HashMapOfString() {
		this(16);
	}

	public HashMapOfString(int size) {
		this.elementSize = 0;
		//table size must always be a power of two
		int tableLen = 1;
		while (tableLen < size)
			tableLen *= 2;
		this.keyTable = new String[tableLen];
		this.valueTable = new String[tableLen];
		this.threshold = (int) (tableLen * LOAD_FACTOR);
	}

	public String get(String key) {
		int lengthMask = keyTable.length - 1;
		int index = key.hashCode() & lengthMask;
		String currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.equals(key))
				return valueTable[index];
			index = (index+1) & lengthMask;
		}
		return null;
	}

	public boolean isEmpty() {
		return elementSize == 0;
	}

	/**
	 * Returns an array of all keys in this map.
	 */
	public String[] keys() {
		if (elementSize == 0)
			return EMPTY_STRING_ARRAY;
		String[] result = new String[elementSize];
		int next = 0;
		for (int i = 0; i < keyTable.length; i++)
			if (keyTable[i] != null)
				result[next++] = keyTable[i];
		return result;
	}

	public String put(String key, String value) {
		int lengthMask = keyTable.length - 1;
		int index = key.hashCode() & lengthMask;
		String currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.equals(key))
				return valueTable[index] = value;
			index = (index+1) & lengthMask;
		}
		keyTable[index] = key;
		valueTable[index] = value;

		// assumes the threshold is never equal to the size of the table
		if (++elementSize > threshold)
			rehash(keyTable.length * 2);
		return value;
	}

	private void rehash(int newLen) {
		HashMapOfString newHashtable = new HashMapOfString(newLen);
		String currentKey;
		int oldLen = keyTable.length;
		for (int i = oldLen; --i >= 0;)
			if ((currentKey = keyTable[i]) != null)
				newHashtable.put(currentKey, valueTable[i]);
		this.keyTable = newHashtable.keyTable;
		this.valueTable = newHashtable.valueTable;
		this.threshold = newHashtable.threshold;
	}

	public String removeKey(String key) {
		int lengthMask = keyTable.length - 1;
		int index = key.hashCode() & lengthMask;
		String currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.equals(key)) {
				String value = valueTable[index];
				elementSize--;
				keyTable[index] = null;
				valueTable[index] = null;
				rehash((int) (elementSize / LOAD_FACTOR));
				return value;
			}
			index = (index+1) & lengthMask;
		}
		return null;
	}

	/* (non-Javadoc
	 * Method declared on IStringPoolParticipant
	 */
	public void shareStrings(StringPool set) {
		//copy elements for thread safety
		String[] array = keyTable;
		if (array == null)
			return;
		for (int i = 0; i < array.length; i++) {
			String o = array[i];
			if (o != null)
				array[i] = set.add(o);
		}
		array = valueTable;
		if (array == null)
			return;
		for (int i = 0; i < array.length; i++) {
			String o = array[i];
			if (o != null)
				array[i] = set.add(o);
		}
	}

	public int size() {
		return elementSize;
	}

	public String toString() {
		String s = ""; //$NON-NLS-1$
		String value;
		for (int i = 0, length = valueTable.length; i < length; i++)
			if ((value = valueTable[i]) != null)
				s += keyTable[i] + " -> " + value.toString() + "\n"; //$NON-NLS-2$ //$NON-NLS-1$
		return s;
	}
}