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

/**
 * An IndexAnchor provides a place to hang index-wide information in a fixed spot, especially
 * since the root node may change due to a root node split.
 */

class IndexAnchor extends IndexedStoreObject {

	public static final int SIZE = 32;
	public static final int TYPE = 1;

	protected static final int RootNodeAddressOffset = 2;
	protected static final int RootNodeAddressLength = 4;

	protected static final int NumberOfEntriesOffset = 14;
	protected static final int NumberOfEntriesLength = 4;
	
	protected Field numberOfEntriesField; 
	protected int numberOfEntries;

	protected Field rootNodeAddressField;
	protected ObjectAddress rootNodeAddress;
	
	/** 
	 * Constructs a new index anchor from nothing.
	 */
	public IndexAnchor() {
		super();
		numberOfEntries = 0;
		rootNodeAddress = ObjectAddress.Null;
	}
	
	/** 
	 * Constructs a new index anchor from a field read from the store.  Used by the factory.
	 */
	public IndexAnchor(Field f, ObjectStore store, ObjectAddress address) throws ObjectStoreException {
		super(f, store, address);
	}

	/**
	 * Sets the fields definitions.  Done after the contents are set.
	 */
	private void setFields(Field f) {
		rootNodeAddressField = f.subfield(RootNodeAddressOffset, RootNodeAddressLength);
		numberOfEntriesField = f.subfield(NumberOfEntriesOffset, NumberOfEntriesLength);
	}

	/**
	 * Places the contents of the fields into the buffer.
	 * Subclasses should implement and call super.
	 */
	protected void insertValues(Field f) {
		super.insertValues(f);
		setFields(f);
		numberOfEntriesField.put(numberOfEntries);
		rootNodeAddressField.put(rootNodeAddress);
	}

	/**
	 * Places the contents of the buffer into the fields.
	 * Subclasses should implement and call super.
	 */
	protected void extractValues(Field f) throws ObjectStoreException {
		super.extractValues(f);
		setFields(f);
		numberOfEntries = numberOfEntriesField.getInt();
		rootNodeAddress = new ObjectAddress(rootNodeAddressField.get());
	}

	/**
	 * Returns the minimum size of this object's instance -- including its type field.
	 * Subclasses should override.
	 */
	protected int getMinimumSize() {
		return SIZE;
	}

	/**
	 * Returns the required type of this class of object.
	 * Subclasses must override.
	 */
	protected int getRequiredType() {
		return TYPE;
	}

	/**
	 * Returns a printable representation of this object.
	 */
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Anchor("); //$NON-NLS-1$
		b.append(numberOfEntries);
		b.append(","); //$NON-NLS-1$
		b.append(rootNodeAddress);
		b.append(")"); //$NON-NLS-1$
		return b.toString();
	}

	/**
	 * Processes the notification that an entry was inserted.
	 */
	void entryInserted(IndexNode node) {
		if (node.isLeaf()) {
			numberOfEntries++;
			setChanged();
		}
	}

	/**
	 * Processes the notification by a leaf node that an entry was removed.
	 */
	void entryRemoved(IndexNode node) {
		if (node.isLeaf()) {
			numberOfEntries--;
			setChanged();
		}
	}

	/**
	 * Sets the root node address.  Set when root node is initialized or split.
	 */
	void setRootNodeAddress(ObjectAddress rootNodeAddress) {
		this.rootNodeAddress = rootNodeAddress;
		setChanged();
	}
	
	/**
	 * This method requests the anchor to destroy its children.
	 */
	void destroyChildren() throws IndexedStoreException {
		IndexNode rootNode = acquireNode(rootNodeAddress);
		rootNode.destroyChildren();
		rootNode.release();
		removeObject(rootNodeAddress);
	}

	/**
	 * This method returns a cursor set to the first entry in the index whose key 
	 * is greater than or equal to the key provided.  To set a cursor to the beginning 
	 * of the index use a key of zero length.
	 */
	void find(byte key[], IndexCursor cursor) throws IndexedStoreException {
		if (rootNodeAddress.isNull()) {
			cursor.reset();
		} else {
			IndexNode rootNode = acquireNode(rootNodeAddress);
			rootNode.find(key, cursor);
			rootNode.release();
		}
	}

	/**
	 * This method returns a cursor set to the first entry in the index.
	 */
	void findFirstEntry(IndexCursor cursor) throws IndexedStoreException {
		if (rootNodeAddress.isNull()) {
			cursor.reset();
		} else {
			IndexNode rootNode = acquireNode(rootNodeAddress);
			rootNode.findFirstEntry(cursor);
			rootNode.release();
		}
	}

	/**
	 * This method returns a cursor set to the last entry in the index.
	 */
	void findLastEntry(IndexCursor cursor) throws IndexedStoreException {
		if (rootNodeAddress.isNull()) {
			cursor.reset();
		} else {
			IndexNode rootNode = acquireNode(rootNodeAddress);
			rootNode.findLastEntry(cursor);
			rootNode.release();
		}
	}

	/**
	 * Insert an entry into an index.  
	 */
	void insert(byte[] key, byte[] value) throws IndexedStoreException {
		if (rootNodeAddress.isNull()) {
			IndexNode rootNode = new IndexNode(this.address);
			try {
				store.insertObject(rootNode);
			} catch (ObjectStoreException e) {
				throw new IndexedStoreException(IndexedStoreException.IndexNodeNotCreated);
			}
			rootNodeAddress = rootNode.getAddress();
		}
		IndexNode rootNode = acquireNode(rootNodeAddress);
		rootNode.insertEntry(key, value);
		rootNode.release();
	}

	/**
	 * Returns the number of entries in the index.
	 */
	int getNumberOfEntries() {
		return numberOfEntries;
	}

	/**
	 * Returns the number of nodes in the index.
	 */
	int getNumberOfNodes() throws IndexedStoreException {
		if (rootNodeAddress.isNull()) return 0;
		IndexNode node = acquireNode(rootNodeAddress);
		int n = node.getNumberOfNodes();
		node.release();
		return n;
	}

	/**
	 * Returns the root node address.
	 */
	ObjectAddress getRootNodeAddress() {
		return rootNodeAddress;
	}

}
