/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.io.*;

/**
 * Hashtable of {String --> int }
 */
public final class HashtableOfStringAndInt implements Cloneable {
	public static final int MISSING_ELEMENT = Integer.MIN_VALUE;

	// to avoid using Enumerations, walk the individual tables skipping nulls
	private String keyTable[];
	private int valueTable[];

	private int elementSize; // number of elements in the table
	private int threshold;
	private static final float GROWTH_FACTOR = 1.33f;

	public HashtableOfStringAndInt() {
		this(13);
	}

	public HashtableOfStringAndInt(int size) {
		this.elementSize = 0;
		this.threshold = size; // size represents the expected number of elements
		int extraRoom = (int) (size * 1.75f);
		if (this.threshold == extraRoom)
			extraRoom++;
		this.keyTable = new String[extraRoom];
		this.valueTable = new int[extraRoom];
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
		//		HashtableOfStringAndInt result = (HashtableOfStringAndInt) super.clone();
		//		result.elementSize = this.elementSize;
		//		result.threshold = this.threshold;
		//
		//		int length = this.keyTable.length;
		//		result.keyTable = new char[length][];
		//		System.arraycopy(this.keyTable, 0, result.keyTable, 0, length);
		//
		//		length = this.valueTable.length;
		//		result.valueTable = new Object[length];
		//		System.arraycopy(this.valueTable, 0, result.valueTable, 0, length);
		//		return result;
	}

	public boolean containsKey(String key) {
		int index = (key.hashCode() & 0x7FFFFFFF) % valueTable.length;
		int keyLength = key.length();
		String currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length() == keyLength && currentKey.equals(key))
				return true;
			index = (index + 1) % keyTable.length;
		}
		return false;
	}

	public int get(String key) {
		int index = (key.hashCode() & 0x7FFFFFFF) % valueTable.length;
		int keyLength = key.length();
		String currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length() == keyLength && currentKey.equals(key))
				return valueTable[index];
			index = (index + 1) % keyTable.length;
		}
		return MISSING_ELEMENT;
	}

	public int put(String key, int value) {
		int index = (key.hashCode() & 0x7FFFFFFF) % valueTable.length;
		int keyLength = key.length();
		String currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length() == keyLength && currentKey.equals(key))
				return valueTable[index] = value;
			index = (index + 1) % keyTable.length;
		}
		keyTable[index] = key;
		valueTable[index] = value;

		// assumes the threshold is never equal to the size of the table
		if (++elementSize > threshold)
			rehash();
		return value;
	}

	public int removeKey(String key) {
		int index = (key.hashCode() & 0x7FFFFFFF) % valueTable.length;
		int keyLength = key.length();
		String currentKey;
		while ((currentKey = keyTable[index]) != null) {
			if (currentKey.length() == keyLength && currentKey.equals(key)) {
				int value = valueTable[index];
				elementSize--;
				keyTable[index] = null;
				valueTable[index] = MISSING_ELEMENT;
				rehash();
				return value;
			}
			index = (index + 1) % keyTable.length;
		}
		return MISSING_ELEMENT;
	}

	private void rehash() {
		HashtableOfStringAndInt newHashtable = new HashtableOfStringAndInt((int) (elementSize * GROWTH_FACTOR));
		String currentKey;
		for (int i = keyTable.length; --i >= 0;)
			if ((currentKey = keyTable[i]) != null)
				newHashtable.put(currentKey, valueTable[i]);

		this.keyTable = newHashtable.keyTable;
		this.valueTable = newHashtable.valueTable;
		this.threshold = newHashtable.threshold;
	}

	public int size() {
		return elementSize;
	}

	public String toString() {
		String s = ""; //$NON-NLS-1$
		int object;
		for (int i = 0, length = valueTable.length; i < length; i++)
			if ((object = valueTable[i]) != MISSING_ELEMENT)
				s += new String(keyTable[i]) + " -> " + object + "\n"; //$NON-NLS-2$ //$NON-NLS-1$
		return s;
	}

	public int[] getValues() {
		int keyTableLength = keyTable.length;
		int[] result = new int[size()];
		int j = 0;
		for (int i = 0; i < keyTableLength; i++) {
			if (keyTable[i] != null)
				result[j++] = valueTable[i];
		}
		return result;
	}

	public void save(DataOutputStream out) throws IOException {
		out.writeInt(elementSize);
		int tableSize = keyTable.length;
		out.writeInt(tableSize);
		out.writeInt(threshold);
		for (int i = 0; i < tableSize; i++) {
			writeStringOrNull(keyTable[i], out);
			out.writeInt(valueTable[i]);
		}
	}

	public void load(DataInputStream in) throws IOException {
		elementSize = in.readInt();
		int tableSize = in.readInt();
		threshold = in.readInt();
		boolean fastMode = true;
		if (((double) tableSize / elementSize) < GROWTH_FACTOR) {	
			keyTable = new String[(int) (elementSize * GROWTH_FACTOR)];
			valueTable = new int[(int) (elementSize * GROWTH_FACTOR)];
			elementSize = 0;
			fastMode = false;
		} else {
			keyTable = new String[tableSize];
			valueTable = new int[tableSize];
		}
		for (int i = 0; i < tableSize; i++) {
			String key = readStringOrNull(in);
			int value = in.readInt();
			if (fastMode) {
				keyTable[i] = key;
				valueTable[i] = value;
			} else {
				if (key != null)
					put(key, value);
			}
		}
	}

	private static final byte NULL = 0;
	private static final byte OBJECT = 1;

	private void writeStringOrNull(String string, DataOutputStream out) throws IOException {
		if (string == null)
			out.writeByte(NULL);
		else {
			out.writeByte(OBJECT);
			out.writeUTF(string);
		}
	}

	private String readStringOrNull(DataInputStream in) throws IOException {
		byte type = in.readByte();
		if (type == NULL)
			return null;
		return in.readUTF();
	}

}
