/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import org.eclipse.core.internal.indexing.*;
import org.eclipse.core.internal.properties.IndexedStoreWrapper;
import org.eclipse.core.internal.utils.Convert;
import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import org.eclipse.core.runtime.*;

/**
 * HistoryStoreEntry objects perform all required conversion operations
 * when performing insertion and retrieval from the history store.
 * Following retrieval, removal from the store is also performed 
 * through the entry objects.
 * <p>
 * For insertion:
 *     - Create an entry object using create(IndexedStore, IndexCursor).
 *     - Insert the entry to the store using the keyToBytes() and
 *       valueToBytes() methods.
 * <p>
 * For retrieval:
 *     - Create an entry object by providing the indexed store and
 *       the cursor location from which version information can be read.
 * <p>
 * A static method keyPrefixToBytes() is provided to perform the 
 * conversion of key prefix values to byte array representation. The 
 * returned value is intended to be used when traversing the indexed 
 * store on partial key matches (path + timestamp) is required.
 * 
 */
public class HistoryStoreEntry implements ILocalStoreConstants {
	private IndexCursor cursor;
	private UniversalUniqueIdentifier uuid;
	private byte[] key;

	/**
	 * Constructs an entry object for retrieval from the history store.
	 */
	private HistoryStoreEntry(byte[] key, byte[] value, IndexCursor cursor) {
		this.cursor = cursor;
		this.key = key;
		this.uuid = new UniversalUniqueIdentifier(value);
	}

	/**
	 * Constructs an entry object to perform insertion to the history store.
	 */
	public HistoryStoreEntry(IPath path, UniversalUniqueIdentifier uuid, long lastModified, byte count) {
		this.key = keyToBytes(path, lastModified, count);
		this.uuid = uuid;
	}

	protected boolean compare(byte[] one, byte[] another) {
		if (one.length != another.length)
			return false;
		for (int i = 0; i < one.length; i++)
			if (one[i] != another[i])
				return false;
		return true;
	}

	/**
	 * Returns an entry object containing the information retrieved from the history store.
	 *
	 * @param store Indexed history store from which data is to be read.
	 * @param cursor Position from which data is to be read.
	 */
	public static HistoryStoreEntry create(IndexedStoreWrapper store, IndexCursor cursor) throws CoreException, IndexedStoreException {
		byte[] key = cursor.getKey();
		ObjectID valueID = cursor.getValueAsObjectID();
		byte[] value = store.getObject(valueID);
		return new HistoryStoreEntry(key, value, cursor);
	}

	public byte getCount() {
		return key[key.length - 1];
	}

	public byte[] getKey() {
		return key;
	}

	public long getLastModified() {
		byte[] lastModifiedBytes = new byte[SIZE_LASTMODIFIED];
		int position = (key.length - SIZE_KEY_SUFFIX);
		System.arraycopy(key, position, lastModifiedBytes, 0, SIZE_LASTMODIFIED);
		return Convert.bytesToLong(lastModifiedBytes);
	}

	public IPath getPath() {
		byte[] pathBytes = new byte[key.length - SIZE_KEY_SUFFIX];
		System.arraycopy(key, 0, pathBytes, 0, pathBytes.length);
		return new Path(Convert.fromUTF8(pathBytes));
	}

	public UniversalUniqueIdentifier getUUID() {
		return uuid;
	}

	/**
	 * Converts the provided parameters into a single byte array representation.
	 * Format:
	 *     path + lastModified.
	 *
	 * @return Converted byte array.
	 */
	public static byte[] keyPrefixToBytes(IPath path, long lastModified) {
		// Retrieve byte array representations of values.
		byte[] pathBytes = Convert.toUTF8(path.toString());
		byte[] lastModifiedBytes = Convert.longToBytes(lastModified);
		// Byte array to hold key prefix.
		byte[] keyPrefixBytes = new byte[pathBytes.length + lastModifiedBytes.length];
		// Copy values.
		System.arraycopy(pathBytes, 0, keyPrefixBytes, 0, pathBytes.length);
		System.arraycopy(lastModifiedBytes, 0, keyPrefixBytes, pathBytes.length, lastModifiedBytes.length);
		return keyPrefixBytes;
	}

	/**
	 * Converts the key for this entry object into byte array representation.
	 * Format:
	 *     path + lastModified + count.
	 *
	 * Note that the count variable consists of a single byte. All other portions 
	 * of the entry consist of multiple bytes.
	 *
	 * @return Key as a byte array.
	 */
	protected byte[] keyToBytes(IPath path, long lastModified, byte count) {
		// Get beginning portion of key.
		byte[] keyPrefix = keyPrefixToBytes(path, lastModified);
		// Byte array to hold full key. The count value is 1 byte in length.
		byte[] keyBytes = new byte[keyPrefix.length + 1];
		// Copy all values into full key.
		int destPosition = 0;
		System.arraycopy(keyPrefix, 0, keyBytes, destPosition, keyPrefix.length);
		destPosition += keyPrefix.length;
		keyBytes[destPosition] = count;
		return keyBytes;
	}

	/**
	 * Removes this entry from the store.
	 */
	public void remove() throws IndexedStoreException {
		if (cursor == null)
			return;
		reposition();
		if (!cursor.isSet())
			return;
		cursor.remove();
	}

	protected void reposition() throws IndexedStoreException {
		if (cursor.isSet())
			if (compare(cursor.getKey(), key))
				return;
		cursor.find(key);
	}

	/**
	 * Used for debug.
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("Path: ").append(getPath()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		s.append("Last Modified: ").append(getLastModified()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		s.append("Count: ").append(getCount()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		s.append("UUID: ").append(uuid.toStringAsBytes()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return s.toString();
	}

	/**
	 * Converts the value for this entry object into byte array representation.
	 * Format:
	 *     uuid.
	 *
	 * @return Value as a byte array.
	 */
	public byte[] valueToBytes() {
		return uuid.toBytes();
	}
}
