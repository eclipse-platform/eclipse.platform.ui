/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.indexing;

import java.util.*;

public class IndexCursor {

	private IndexedStore store;
	private ObjectAddress anchorAddress;
	private int entryNumber;
	private IndexNode leafNode;
	private boolean entryRemoved;

	/**
	 * Default constructor for an IndexCursor.
	 */
	private IndexCursor() {
		super();
	}
	/**
	 * Constructor for an IndexCursor.  Cursors should only be constructed
	 * by the index during an open operation.
	 */
	IndexCursor(IndexedStore store, ObjectAddress anchorAddress) {
		this.anchorAddress = anchorAddress;
		this.store = store;
		this.leafNode = null;
		this.entryNumber = -1;
	}
/**
 * Adjusts the position of a cursor to point to a "real" entry
 * entry if it is pointing outside of the entries of a node.
 * If there are no more entries then unset the cursor.
 */
private void adjust() throws IndexedStoreException {
	if (leafNode == null)
		return;
	if (entryNumber >= leafNode.getNumberOfEntries()) {
		ObjectAddress next = leafNode.getNextAddress();
		int n = entryNumber - leafNode.getNumberOfEntries();
		set(next, n);
	} else if (entryNumber < 0) {
		ObjectAddress previous = leafNode.getPreviousAddress();
		int n = entryNumber;
		set(previous, n);
	} else {
	}
}
	/**
	 * Closes the cursor.  This unsets the cursor and deregisters it from all the
	 * interested parties.
	 */
	public void close() throws IndexedStoreException {
		reset();
	}
	/**
	 * Adjusts a cursor if there is a need after an entry is inserted.
	 * If not, it just returns.
	 */
	void entryInserted(int i) throws IndexedStoreException {
		if (entryNumber >= i) entryNumber++;
		adjust();
	}
	/**
	 * Adjusts a cursor if there is a need after an entry is removed.
	 */
	void entryRemoved(int i) throws IndexedStoreException {
		entryRemoved = (entryNumber == i);
		if (entryNumber > i) entryNumber--;
		adjust();
	}
	/**
	 * Sets the cursor at the first entry of an index whose key is 
	 * greater than or equal to that of the argument.  Returns the cursor itself
	 * for convenience in chaining method invocations.
	 */
	public synchronized IndexCursor find(byte[] b) throws IndexedStoreException {
		IndexAnchor anchor = store.acquireAnchor(anchorAddress);
		anchor.find(b, this);
		anchor.release();
		entryRemoved = false;
		return this;
	}
	/**
	 * Sets the cursor at the first entry of an index whose key is 
	 * greater than or equal to that of the argument.  Returns the cursor itself
	 * for convenience in chaining method invocations.
	 */
	public synchronized IndexCursor find(String s) throws IndexedStoreException {
		return find(Convert.toUTF8(s));
	}
	/**
	 * Sets the cursor at the first entry of an index whose key is 
	 * greater than or equal to that of the argument.  Returns the cursor itself
	 * for convenience in chaining method invocations.
	 */
	public synchronized IndexCursor find(Insertable i) throws IndexedStoreException {
		return find(i.toByteArray());
	}
	/**
	 * Sets the cursor at the first entry of an index.
	 */
	public synchronized IndexCursor findFirstEntry() throws IndexedStoreException {
		IndexAnchor anchor = store.acquireAnchor(anchorAddress);
		anchor.findFirstEntry(this);
		anchor.release();
		entryRemoved = false;
		return this;
	}
	/**
	 * Sets the cursor at the last entry of an index.
	 */
	public synchronized IndexCursor findLastEntry() throws IndexedStoreException {
		IndexAnchor anchor = store.acquireAnchor(anchorAddress);
		anchor.findLastEntry(this);
		anchor.release();
		entryRemoved = false;
		return this;
	}
	/**
	 * Returns the byte array holding the key for the current cursor location.  
	 * If the cursor is at the beginning or end of the index then return null.
	 * 
	 * Throws an EntryRemoved condition if the entry at which it has
	 * been pointing has been removed by another cursor.
	 */
	public synchronized byte[] getKey() throws IndexedStoreException {
		if (entryRemoved) throw new IndexedStoreException(IndexedStoreException.EntryRemoved);
		if (leafNode == null) return null;
		byte[] key = leafNode.getKey(entryNumber);
		return key;
	}
	/**
	 * Returns the key at the cursor as a string.
	 * If the cursor is at the beginning or end of the index then return null.
	 */
	public synchronized String getKeyAsString() throws IndexedStoreException {
		byte[] key = getKey();
		if (key == null)
			return null;
		String s = Convert.fromUTF8(key);
		int i = s.indexOf(0);
		if (i == -1)
			return s;
		return s.substring(0, i);
	}
	/**
	 * Returns the byte array holding the value for the current cursor location.  If the cursor is
	 * at the beginning or end of the index then return null.
	 * 
	 * Throws an EntryRemoved condition if the entry at which it has
	 * been pointing has been removed by another cursor.
	 */
	public synchronized byte[] getValue() throws IndexedStoreException {
		if (entryRemoved) throw new IndexedStoreException(IndexedStoreException.EntryRemoved);
		if (leafNode == null) return null;
		byte[] value = leafNode.getValue(entryNumber);
		return value;
	}
	/** 
	 * Returns the value as an object address.  May return null if the cursor is at the beginning
	 * or end of the index.
	 */
	ObjectAddress getValueAsObjectAddress() throws IndexedStoreException {
		byte[] value = getValue();
		if (value == null)
			return null;
		return new ObjectAddress(value);
	}
	/**
	 * Returns the ObjectID from the value for the current cursor location.  
	 * If the cursor is at the beginning or end of the index then return null.
	 */
	public synchronized ObjectID getValueAsObjectID() throws IndexedStoreException {
		byte[] value = getValue();
		if (value == null)
			return null;
		return new ObjectID(value);
	}
	/**
	 * Returns the String from the value for the current cursor location.  
	 * If the cursor is at the beginning or end of the index then return null.
	 */
	public synchronized String getValueAsString() throws IndexedStoreException {
		byte[] value = getValue();
		if (value == null)
			return null;
		return Convert.fromUTF8(value);
	}
	/**
	 * This method returns true if the current cursor location before the first entry in the index.
	 */
	public synchronized boolean isAtBeginning() throws IndexedStoreException {
		if (entryRemoved) throw new IndexedStoreException(IndexedStoreException.EntryRemoved);
		return (leafNode == null);
	}
	/**
	 * This method returns true if the current cursor location after the last entry in the index.
	 */
	public synchronized boolean isAtEnd() throws IndexedStoreException {
		if (entryRemoved) throw new IndexedStoreException(IndexedStoreException.EntryRemoved);
		return (leafNode == null);
	}
	/**
	 * Returns true if the cursor is set to an entry.
	 * Returns false otherwise.
	 */
	public synchronized boolean isSet() throws IndexedStoreException {
		if (entryRemoved) throw new IndexedStoreException(IndexedStoreException.EntryRemoved);
		return !(leafNode == null);
	}
	/**
	 * Compares a byte array to the key in the cursor and 
	 * returns true if the byte array is equal to the key at the entry in the cursor.
	 * 
	 * Throws an EntryRemoved condition if the entry at which it has
	 * been pointing has been removed by another cursor.
	 */
	public synchronized boolean keyEquals(byte[] b) throws IndexedStoreException {
		if (entryRemoved) throw new IndexedStoreException(IndexedStoreException.EntryRemoved);
		if (leafNode == null) return false;
		byte[] key = leafNode.getKey(entryNumber);
		if (b.length != key.length) {
			return false;
		}
		for (int i = 0; i < b.length; i++) {
			if (key[i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Compares a String to the key in the cursor and 
	 * returns true if the String is equal to the key at the entry in the cursor.
	 */
	public synchronized boolean keyEquals(String s) throws IndexedStoreException {
		return keyEquals(Convert.toUTF8(s));
	}
	/**
	 * Compares an Insertable to the key in the cursor and 
	 * returns true if the String is equal to the key at the entry in the cursor.
	 */
	public synchronized boolean keyEquals(Insertable anObject) throws IndexedStoreException {
		return keyEquals(anObject.toByteArray());
	}
	/**
	 * Compares a byte array to the key in the cursor and 
	 * returns true if the byte array is a prefix
	 * of the key at the entry in the cursor.
	 * 
	 * Throws an EntryRemoved condition if the entry at which it has
	 * been pointing has been removed by another cursor.
	 */
	public synchronized boolean keyMatches(byte[] b) throws IndexedStoreException {
		if (entryRemoved) throw new IndexedStoreException(IndexedStoreException.EntryRemoved);
		if (leafNode == null) return false;
		byte[] key = leafNode.getKey(entryNumber);
		if (key.length < b.length) {
			return false;
		}
		for (int i = 0; i < b.length; i++) {
			if (key[i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Compares a String to the key in the cursor and 
	 * returns true if the byte array is a prefix
	 * of the key at the entry in the cursor.
	 */
	public synchronized boolean keyMatches(String s) throws IndexedStoreException {
		return keyMatches(Convert.toUTF8(s));
	}
	/**
	 * Compares an Insertable to the key in the cursor and 
	 * returns true if the byte array is a prefix
	 * of the key at the entry in the cursor.
	 */
	public synchronized boolean keyMatches(Insertable anObject) throws IndexedStoreException {
		return keyMatches(anObject.toByteArray());
	}
	/**
	 * Moves the cursor to the next index entry.  
	 * If the cursor is at the last entry, it becomes unset.
	 * If the cursor is unset, then it is set to the first entry.
	 * The cursor itself is returned.
	 * 
	 * Throws an EntryRemoved condition if the entry at which it has
	 * been pointing has been removed by another cursor.
	 */
	public synchronized IndexCursor next() throws IndexedStoreException {
		if (isAtBeginning()) {
			findFirstEntry();
		} else {
			entryNumber++;
			adjust();
		}
		return this;
	}
	/**
	 * Adjusts a cursor if there is a need after a node has been split.
	 * If not, it just returns.
	 */
	void nodeSplit() throws IndexedStoreException {
		adjust();
	}
	/**
	 * Moves the cursor to the previous index entry.  
	 * If the cursor is at the first entry, it becomes unset.
	 * If the cursor is unset, then it is set to the last entry.
	 * The cursor itself is returned.
	 * 
	 * Throws an EntryRemoved condition if the entry at which it has
	 * been pointing has been removed by another cursor.
	 */
	public synchronized IndexCursor previous() throws IndexedStoreException {
		if (isAtEnd()) {
			findLastEntry();
		} else {
			entryNumber--;
			adjust();
		}
		return this;
	}
	/**
	 * Removes the entry at the current cursor location.  If the cursor is not set
	 * then no operation is done.  If an element is removed
	 * the cursor automatically advances to the "next" element.  
	 * Removing an element adjusts all cursors (including this one) pointing into this node.
	 * If there is no next element, the cursor is unset.
	 * 
	 * Throws an EntryRemoved condition if the entry at which it has
	 * been pointing has been removed by another cursor.
	 */
	public synchronized void remove() throws IndexedStoreException {
		removeEntry();
	}
/**
 * Removes the entry at the current cursor location.  If the cursor is not set
 * then no operation is done.  If an element is removed
 * the cursor automatically advances to the "next" element.  
 * Removing an element adjusts all cursors (including this one) pointing into this node.
 * If there is no next element, the cursor is unset.
 * 
 * Throws an EntryRemoved condition if the entry at which it has
 * been pointing has been removed by another cursor.
 */
void removeEntry() throws IndexedStoreException {
	if (entryRemoved)
		throw new IndexedStoreException(IndexedStoreException.EntryRemoved);
	if (leafNode == null)
		return;
	ObjectAddress address = leafNode.getAddress();
	leafNode.removeEntry(entryNumber);
	entryRemoved = false; // Clear the flag. This cursor is positioned to the next entry and remains valid.

	/* remove empty nodes from the tree */
	while (!address.isNull()) {
		IndexNode node = store.acquireNode(address);
		if (node.getNumberOfEntries() > 0) {
			node.release();
			break;
		}
		ObjectAddress parentAddress = node.getParentAddress();
		node.unlink();
		node.release();
		store.removeObject(address);
		address = parentAddress;
	}
}
	/**
	 * Places the cursor in the "unset" state.
	 */
	public synchronized void reset() throws IndexedStoreException {
		unset();
		entryRemoved = false;
	}
/**
 * Sets the cursor to a particular entry of an index node.
 */
void set(ObjectAddress leafNodeAddress, int entryNumber) throws IndexedStoreException {
	unset();
	if (leafNodeAddress.isNull()) return;
	leafNode = store.acquireNode(leafNodeAddress);
	leafNode.addCursor(this);
	if (entryNumber >= 0)
		this.entryNumber = entryNumber;
	else
		this.entryNumber = leafNode.getNumberOfEntries() + entryNumber;
	adjust();
}
	/**
	 * Places the cursor in the "unset" state.
	 */
	private void unset() throws IndexedStoreException {
		if (leafNode != null) {
			leafNode.removeCursor(this);
			leafNode.release();
		}
		entryNumber = -1;
		leafNode = null;
		entryRemoved = false;
	}
	/**
	 * Updates the value of the index entry at the cursor.
	 * If the cursor is at the beginning or end of the index then do nothing.
	 * Returns true if the value is set, false otherwise.
	 */
	void updateEntry(byte[] b) throws IndexedStoreException {
		if (entryRemoved) throw new IndexedStoreException(IndexedStoreException.EntryRemoved);
		if (b.length > 2048)
			throw new IndexedStoreException(IndexedStoreException.EntryValueLengthError);
		if (leafNode == null) return;
		leafNode.updateValueAt(entryNumber, b);
	}
	/**
	 * Updates the value of the index entry at the cursor.
	 * If the cursor is at the beginning or end of the index then do nothing.
	 * Returns true if the value is set, false otherwise.
	 * 
	 * Throws an EntryRemoved condition if the entry at which it has
	 * been pointing has been removed by another cursor.
	 */
	public synchronized void updateValue(byte[] b) throws IndexedStoreException {
		updateEntry(b);
	}
	/**
	 * Updates the value of the index entry at the cursor.
	 * If the cursor is at the beginning or end of the index then do nothing.
	 */
	public synchronized void updateValue(String s) throws IndexedStoreException {
		updateValue(Convert.toUTF8(s));
	}
	/**
	 * Updates the value of the index entry at the cursor.
	 * If the cursor is at the beginning or end of the index then do nothing.
	 */
	public synchronized void updateValue(Insertable anObject) throws IndexedStoreException {
		updateValue(anObject.toByteArray());
	}
}
