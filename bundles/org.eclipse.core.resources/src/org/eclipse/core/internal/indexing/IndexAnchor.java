package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * An IndexAnchor provides a place to hang index-wide information in a fixed spot, especially
 * since the root node may change due to a root node split.
 */

class IndexAnchor extends IndexedStoreObject {

	private static final int Size = 32;
	private static final int Type = 1;

	private static final int RootNodeAddressOffset = 2;
	private static final int RootNodeAddressLength = 4;
	private Field rootNodeAddressField;
	private ObjectAddress rootNodeAddress;

	private static final int NumberOfEntriesOffset = 14;
	private static final int NumberOfEntriesLength = 4;
	private Field numberOfEntriesField;
	private int numberOfEntries;
	
/** 
 * Constructs a new index anchor from nothing.
 */
IndexAnchor() {
	setContents(new Field(Size));
	type = getRequiredType();
	numberOfEntries = 0;
	rootNodeAddress = ObjectAddress.Null;
}
/** 
 * Constructs a new index anchor from a field read from the store.  Used by the factory.
 */
IndexAnchor(Field f) throws ObjectStoreException {
	super(f);
}
/**
 * Places the contents of the fields into the buffer.
 * Subclasses should implement and call super.
 */
protected void dematerialize() {
	numberOfEntriesField.put(numberOfEntries);
	rootNodeAddressField.put(rootNodeAddress);
	super.dematerialize();
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
 * Processes the notification that an entry was inserted.
 */
void entryInserted(IndexNode node) {
	if (node.isLeaf()) {
		numberOfEntries++;
		modified();
	}
}
/**
 * Processes the notification by a leaf node that an entry was removed.
 */
void entryRemoved(IndexNode node) {
	if (node.isLeaf()) {
		numberOfEntries--;
		modified();
	}
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
 * Returns the minimum size of this object's instance -- including its type field.
 * Subclasses should override.
 */
protected int getMinimumSize() {
	return Size;
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
	if (rootNodeAddress.isNull())
		return 0;
	IndexNode node = acquireNode(rootNodeAddress);
	int n = node.getNumberOfNodes();
	node.release();
	return n;
}
/**
 * Returns the required type of this class of object.
 * Subclasses must override.
 */
protected int getRequiredType() {
	return Type;
}
/**
 * Returns the root node address.
 */
ObjectAddress getRootNodeAddress() {
	return rootNodeAddress;
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
 * Places the contents of the buffer into the fields.
 * Subclasses should implement and call super.
 */
protected void materialize() throws ObjectStoreException {
	super.materialize();
	numberOfEntries = numberOfEntriesField.getInt();
	rootNodeAddress = new ObjectAddress(rootNodeAddressField.get());
}
/**
 * Registers the factory for this type.
 */
static void registerFactory() {
	ObjectStore.registerFactory(Type, new IndexAnchorFactory());
}
/**
 * Sets the fields definitions.  Done after the contents are set.
 */
protected void setFields() {
	super.setFields();
	rootNodeAddressField = contents.getSubfield(RootNodeAddressOffset, RootNodeAddressLength);
	numberOfEntriesField = contents.getSubfield(NumberOfEntriesOffset, NumberOfEntriesLength);
}
/**
 * Sets the root node address.  Set when root node is initialized or split.
 */
void setRootNodeAddress(ObjectAddress rootNodeAddress) {
	this.rootNodeAddress = rootNodeAddress;
	modified();
}
/**
 * Returns a printable representation of this object.
 */
public String toString() {
	StringBuffer b = new StringBuffer();
	b.append("Anchor(");
	if (contents != null) {
		b.append(getNumberOfEntries());
		b.append(",");
		b.append(getRootNodeAddress());
	}
	b.append(")");
	return b.toString();
}
}
