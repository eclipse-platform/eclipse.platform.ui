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
package org.eclipse.core.internal.indexing;

import java.util.Vector;

/**
 * This class provides the public interface to an index.
 */
public class Index {

	private IndexedStore store;
	private ObjectAddress anchorAddress;

	/**
	 * Index constructor.
	 */
	Index(IndexedStore store, ObjectAddress anchorAddress) {
		this.store = store;
		this.anchorAddress = anchorAddress;
	}

	/**
	 * Returns a vector of ObjectIDs whose keys match the key given in the index.  
	 * This assumes that the underlying index has values that can be converted 
	 * to ObjectIDs.
	 */
	public synchronized Vector getObjectIdentifiersMatching(byte[] key) throws IndexedStoreException {
		IndexCursor cursor = open();
		cursor.find(key);
		Vector vector = new Vector(20);
		while (cursor.keyMatches(key)) {
			vector.addElement(cursor.getValueAsObjectID());
			cursor.next();
		}
		cursor.close();
		return vector;
	}

	/**
	 * Inserts an entry into an index.  The key and the value are byte arrays.  
	 * Keys cannot be more than 1024 bytes in length.  Values must not 
	 * be greater than 2048 bytes in length.  The other insert methods are 
	 * convenience methods that use this for their implementation.
	 */
	public synchronized void insert(byte[] key, byte[] value) throws IndexedStoreException {
		if (key.length > 1024)
			throw new IndexedStoreException(IndexedStoreException.EntryKeyLengthError);
		if (value.length > 2048)
			throw new IndexedStoreException(IndexedStoreException.EntryValueLengthError);
		IndexAnchor anchor = store.acquireAnchor(anchorAddress);
		anchor.insert(key, value);
		anchor.release();
	}

	public synchronized void insert(byte[] key, Insertable value) throws IndexedStoreException {
		insert(key, value.toByteArray());
	}

	public synchronized void insert(String key, byte[] value) throws IndexedStoreException {
		insert(Convert.toUTF8(key), value);
	}

	/**
	 * Returns a cursor for this index.  The cursor is initially in the unset state
	 * and should be positioned using "find" before being used.
	 */
	public synchronized IndexCursor open() throws IndexedStoreException {
		IndexCursor c = new IndexCursor(store, anchorAddress);
		return c;
	}
}
