package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

class IndexNode extends IndexedStoreObject {

	/* needed for all stored objects */
	private static final int Size = ObjectStore.MaximumObjectSize;
	private static final int Type = 3;

	/* field definitions */
	private static final int NodeTypeOffset = 2;
	private static final int NodeTypeLength = 2;
	private Field nodeTypeField;
	private int nodeType;

	private static final int AnchorAddressOffset = 4;
	private static final int AnchorAddressLength = 4;
	private Field anchorAddressField;
	private ObjectAddress anchorAddress;

	private static final int ParentAddressOffset = 8;
	private static final int ParentAddressLength = 4;
	private Field parentAddressField;
	private ObjectAddress parentAddress;

	private static final int PreviousAddressOffset = 12;
	private static final int PreviousAddressLength = 4;
	private Field previousAddressField;
	private ObjectAddress previousAddress;

	private static final int NextAddressOffset = 16;
	private static final int NextAddressLength = 4;
	private Field nextAddressField;
	private ObjectAddress nextAddress;

	private static final int NumberOfEntriesOffset = 20;
	private static final int NumberOfEntriesLength = 2;
	private Field numberOfEntriesField;
	private int numberOfEntries;

	private static final int UsedSpaceOffset = 22;
	private static final int UsedSpaceLength = 2;
	private Field usedSpaceField;
	private int usedSpace;

	private static final int UsedSpaceMaxOffset = 24;
	private static final int UsedSpaceMaxLength = 2;
	private Field usedSpaceMaxField;
	private int usedSpaceMax;

	private static final int EntriesOffset = 64;
	private static final int EntriesLength = Size - EntriesOffset;
	private Field entriesField;

	/* types of nodes in the index */
	private static final int RootNode = 1;
	private static final int InteriorNode = 2;
	private static final int LeafNode = 3;

	/* miscellaneous constants */
	private static final int DescriptorLength = 6;
	
	/* cursors in this node */
	private HashSet cursors = new HashSet();
	
/**
 * Reconstructs a node from a field.
 */
IndexNode(Field f) throws ObjectStoreException {
	super(f);
}
/**
 * Constructor that creates a root node.
 */
IndexNode(ObjectAddress anchorAddress) {
	setContents(new Field(getMinimumSize()));
	this.anchorAddress = anchorAddress;
	this.parentAddress = ObjectAddress.Null;
	this.previousAddress = ObjectAddress.Null;
	this.nextAddress = ObjectAddress.Null;
	this.usedSpace = 0;
	this.usedSpaceMax = 0;
	this.numberOfEntries = 0;
	this.nodeType = RootNode;
}
/** 
 * Constructor that creates an interior node.
 */
IndexNode(ObjectAddress anchorAddress, ObjectAddress parentAddress) {
	this(anchorAddress);
	this.parentAddress = parentAddress;
	this.nodeType = InteriorNode;
}
/**
 * Constructor that creates a leaf node.
 */
IndexNode(ObjectAddress anchorAddress, ObjectAddress parentAddress, ObjectAddress previousAddress, ObjectAddress nextAddress) {
	this(anchorAddress, parentAddress);
	this.previousAddress = previousAddress;
	this.nextAddress = nextAddress;
	this.nodeType = LeafNode;
}
/**
 * Registers a cursor with this node.
 */
void addCursor(IndexCursor cursor) {
	cursors.add(cursor);
}
/**
 * Compares the key at a particular entry to a byte array. 
 */
private int compareEntryToKey(int entryNumber, byte[] key) throws IndexedStoreException {
	Field keyField = new Field(key);
	Field entryKeyField = getKeyField(entryNumber);
	int result = entryKeyField.compareTo(keyField);
	return result;
}
/**
 * Compresses the space in the entries area of the node.
 */
private void compress() throws IndexedStoreException {

	/* some preliminaries */
	int entriesLength = entriesField.length();
	int descriptorBlockSize = numberOfEntries * DescriptorLength;

	/* need to make a copy of the entries in the area, this will compress it */
	Field f2 = new Field(entriesField.length());
	copyEntries(entriesField, 0, numberOfEntries, f2);

	/* copy the entries area back to the node and modify the usedSpaceMax to reflect the compression */
	entriesField.put(f2.get());
	usedSpaceMax = usedSpace;

	/* clear the space in the between the descriptor array and the entries heap */
	int freeBlockSize = entriesLength - (descriptorBlockSize + usedSpaceMax);
	Field f3 = entriesField.getSubfield(descriptorBlockSize, freeBlockSize);
	f3.clear();
	modified();
}
/**
 * Compresses the space in the entries area of the node if the free space block
 * is smaller than the given threshold.
 */
private void compress(int threshold) throws IndexedStoreException {
	int entriesLength = entriesField.length();
	int descriptorBlockSize = numberOfEntries * DescriptorLength;
	int freeBlockSize = entriesLength - (descriptorBlockSize + usedSpaceMax);
	if (freeBlockSize >= threshold)
		return;
	compress();
}
/**
 * Copies entries from one Field to another.  Fields are assumed to contain an array of descriptors at the 
 * low end and a heap of (key,value) pairs at the high end.
 */
private static int copyEntries(Field sourceField, int sourceIndex, int numberOfEntries, Field targetField) {

	Pointer tDescriptor = targetField.pointTo(0);
	Pointer sDescriptor = sourceField.pointTo(sourceIndex * DescriptorLength);
	int tEntryOffset = targetField.length();

	// for each descriptor make a new one in the new area and copy its (key,value) entry
	for (int i = 0; i < numberOfEntries; i++) {

		// extract information from old descriptor
		int sEntryOffset = sDescriptor.getField(0, 2).getUInt();
		int keyLength = sDescriptor.getField(2, 2).getUInt();
		int valueLength = sDescriptor.getField(4, 2).getUInt();
		int entryLength = keyLength + valueLength;
		Field sEntry = sourceField.getSubfield(sEntryOffset, entryLength);

		// copy the (key,value) entry from the old to the new space
		tEntryOffset -= entryLength;
		Field tEntry = targetField.getSubfield(tEntryOffset, entryLength);
		tEntry.put(sEntry.get());

		// create a new descriptor
		tDescriptor.getField(0, 2).put(tEntryOffset);
		tDescriptor.getField(2, 2).put(keyLength);
		tDescriptor.getField(4, 2).put(valueLength);

		// on to the next one
		tDescriptor.inc(DescriptorLength);
		sDescriptor.inc(DescriptorLength);
	}
	return targetField.length() - tEntryOffset;
}
/**
 * Places the contents of the fields into the buffer.
 * Subclasses should implement and call super.
 */
protected void dematerialize() {
	anchorAddressField.put(anchorAddress);
	parentAddressField.put(parentAddress);
	nextAddressField.put(nextAddress);
	previousAddressField.put(previousAddress);
	nodeTypeField.put(nodeType);
	numberOfEntriesField.put(numberOfEntries);
	usedSpaceField.put(usedSpace);
	usedSpaceMaxField.put(usedSpaceMax);
	super.dematerialize();
}
/**
 * Causes the node to remove its children from the store.
 */
void destroyChildren() throws IndexedStoreException {
	if (!isLeaf()) {
		for (int i = 0; i < numberOfEntries; i++) {
			ObjectAddress childNodeAddress = new ObjectAddress(getValue(i));
			IndexNode childNode = acquireNode(childNodeAddress);
			childNode.destroyChildren();
			childNode.release();
			removeObject(childNodeAddress);
		}
	}
}
/**
 * Places a cursor and the first entry greater than or equal to a key.
 */
void find(byte[] key, IndexCursor cursor) throws IndexedStoreException {
	int i;
	i = findLastEntryLT(key);
	if (isLeaf()) {
		cursor.set(address, i + 1);
	} else if (i >= 0) {
		IndexNode childNode = acquireNode(new ObjectAddress(getValue(i)));
		childNode.find(key, cursor);
		childNode.release();
	} else if (numberOfEntries > 0) {
		IndexNode childNode = acquireNode(new ObjectAddress(getValue(0)));
		childNode.find(key, cursor);
		childNode.release();
	} else {
		cursor.reset();
	}
}
/**
 * Places a cursor at the first entry of a node.
 */
void findFirstEntry(IndexCursor cursor) throws IndexedStoreException {
	int i;
	if (numberOfEntries == 0) {
		cursor.reset();
	} else if (!isLeaf()) {
		IndexNode childNode = acquireNode(new ObjectAddress(getValue(0)));
		childNode.findFirstEntry(cursor);
		childNode.release();
	} else {
		cursor.set(address, 0);
	}
}
/**
 * Returns the index of the first entry greater than a key.
 */
private int findFirstEntryGT(byte[] key) throws IndexedStoreException {
	int lo = 0;
	int hi = numberOfEntries - 1;
	while (lo <= hi) {
		int i = (lo + hi) / 2;
		int c = compareEntryToKey(i, key);
		if (c <= 0) {
			lo = i + 1;
		} else {
			hi = i - 1;
		}
	}
	return lo;
}
/**
 * Places a cursor at the last entry of a node.
 */
void findLastEntry(IndexCursor cursor) throws IndexedStoreException {
	if (numberOfEntries == 0) {
		cursor.reset();
		return;
	}
	int i = numberOfEntries - 1;
	if (!isLeaf()) {
		IndexNode childNode = acquireNode(new ObjectAddress(getValue(i)));
		childNode.findLastEntry(cursor);
		childNode.release();
	} else {
		cursor.set(address, i);
	}
}
/**
 * Returns the index of the last entry less than a key.
 */
private int findLastEntryLT(byte[] key) throws IndexedStoreException {
	int lo = 0;
	int hi = numberOfEntries - 1;
	while (lo <= hi) {
		int i = (lo + hi) / 2;
		int c = compareEntryToKey(i, key);
		if (c < 0) {
			lo = i + 1;
		} else {
			hi = i - 1;
		}
	}
	return hi;
}
ObjectAddress getAnchorAddress() {
	return anchorAddress;
}
/**
 * Returns the descriptor field for the node entry at a given index.
 */
private Field getDescriptor(int i) {
	return entriesField.getSubfield(i * DescriptorLength, DescriptorLength);
}
/**
 * Returns the entire array of entry descriptors.
 */
private FieldArray getDescriptorArray() {
	return entriesField.pointTo(0).getArray(DescriptorLength, DescriptorLength, numberOfEntries);
}
private Field getEntriesField() {
	return entriesField;
}
/**
 * Returns the value of the key for an entry at a given index.
 */
byte[] getKey(int i) {
	return getKeyField(i).get();
}
/**
 * Returns a Field covering the key for an entry at a given index.
 */
private Field getKeyField(int i) {
	Field descriptor = getDescriptor(i);
	int keyOffset = descriptor.getSubfield(0, 2).getUInt();
	int keyLength = descriptor.getSubfield(2, 2).getUInt();
	return entriesField.getSubfield(keyOffset, keyLength);
}
/**
 * Returns a Field covering the (key, value) pair for an entry at a given index.
 */
private Field getKeyValueField(int i) {
	Field descriptor = getDescriptor(i);
	int offset = descriptor.getSubfield(0, 2).getUInt();
	int keyLength = descriptor.getSubfield(2, 2).getUInt();
	int valueLength = descriptor.getSubfield(4, 2).getUInt();
	return entriesField.getSubfield(offset, keyLength + valueLength);
}
/**
 * Returns the lowest key in the node.  If none, returns an empty byte arrray.
 */
private byte[] getLowKey() {
	if (numberOfEntries == 0)
		return new byte[0];
	return getKey(0);
}
/**
 * Returns the minimum size of this object's instance -- including its type field.
 * Subclasses should override.
 */
protected int getMinimumSize() {
	return Size;
}
ObjectAddress getNextAddress() {
	return nextAddress;
}
private int getNodeType() {
	return nodeType;
}
int getNumberOfEntries() {
	return numberOfEntries;
}
/**
 * Returns the number of nodes in this subtree (this one plus all descendants).
 */
int getNumberOfNodes() throws IndexedStoreException {
	if (isLeaf())
		return 1;
	int sum = 0;
	for (int i = 0; i < numberOfEntries; i++) {
		ObjectAddress childAddress = new ObjectAddress(getValue(i));
		IndexNode childNode = acquireNode(childAddress);
		sum += childNode.getNumberOfNodes();
		childNode.release();
	}
	return sum + 1;
}
ObjectAddress getParentAddress() {
	return parentAddress;
}
ObjectAddress getPreviousAddress() {
	return previousAddress;
}
/**
 * Returns the required type of this class of object.
 * Subclasses must override.
 */
protected int getRequiredType() {
	return Type;
}
private int getUsedSpace() {
	return usedSpace;
}
private int getUsedSpaceMax() {
	return usedSpaceMax;
}
/**
 * Returns the value for an entry at a given index.
 */
byte[] getValue(int i) {
	return getValueField(i).get();
}
/**
 * Returns a Field covering the value for an entry at a given index.
 */
private Field getValueField(int i) {
	Field descriptor = getDescriptor(i);
	int keyOffset = descriptor.getSubfield(0, 2).getUInt();
	int keyLength = descriptor.getSubfield(2, 2).getUInt();
	int valueLength = descriptor.getSubfield(4, 2).getUInt();
	int valueOffset = keyOffset + keyLength;
	return entriesField.getSubfield(valueOffset, valueLength);
}
/**
 * Inserts an entry into the node.  If this was inserted in slot 0, then we must update the parent
 * node's low key.  If this was a leaf node then we must update the anchor'ss number of entries and
 * adjust any cursors for this node.  This may also cause a split.
 *
 * Implementation Note: Cannot use an iterator over the cursor set because 
 * notification of an insert may remove the cursor being notified from the cursor set.
 */
void insertEntry(byte[] key, byte[] value) throws IndexedStoreException {
	int i = findFirstEntryGT(key);
	if (isLeaf()) {
		insertEntryBefore(i, key, value);
		Object[] cursorArray = cursors.toArray();
		for (int j = 0; j < cursorArray.length; j++) {
			IndexCursor cursor = (IndexCursor) cursorArray[j];
			cursor.entryInserted(i);
		}
		IndexAnchor anchor = acquireAnchor(anchorAddress);
		anchor.entryInserted(this);
		anchor.release();
	} else {
		ObjectAddress childNodeAddress = null;
		if (getNumberOfEntries() == 0) {
			IndexNode childNode = new IndexNode(anchorAddress, address, ObjectAddress.Null, ObjectAddress.Null);
			childNodeAddress = insertObject(childNode);
		} else {
			childNodeAddress = new ObjectAddress(getValue(Math.max(0, (i - 1))));
		}
		IndexNode childNode = acquireNode(childNodeAddress);
		childNode.insertEntry(key, value);
		childNode.release();
	}
}
/**
 * Inserts a new (key, value) pair in front of the entry at the given index.
 * If the node needs to be split, split it and then attempt the insert again.  If this is a
 * non-leaf node then the value is the address of a child.  That child's parent address
 * will be updated if that (key, value) is to be inserted into a new node.
 */
private void insertEntryBefore(int i, byte[] key, byte[] value) throws IndexedStoreException {
	Field entries = entriesField;
	int entriesLength = entries.length();
	int keyValueLength = key.length + value.length;
	int neededSpace = keyValueLength + DescriptorLength;
	int freeSpace = entriesLength - ((numberOfEntries * DescriptorLength) + usedSpace);
	if (freeSpace < neededSpace) {
		ObjectAddress newNodeAddress = split();
		if (i > numberOfEntries) {
			if (!isLeaf()) {
				ObjectAddress childAddress = new ObjectAddress(value);
				IndexNode child = acquireNode(childAddress);
				child.setParentAddress(newNodeAddress);
				child.release();
			}
			IndexNode newNode = acquireNode(newNodeAddress);
			newNode.insertEntryBefore(i - getNumberOfEntries(), key, value);
			newNode.release();
		} else {
			insertEntryBefore(i, key, value);
		}
		return;
	}

	/* place the value and key fields into the space */
	compress(neededSpace);
	Pointer p = entries.pointTo(entriesLength - usedSpaceMax);
	p.dec(value.length).put(value);
	p.dec(key.length).put(key);
	usedSpaceMax += keyValueLength;
	usedSpace += keyValueLength;

	/* create a hole in the descriptor area for a new descriptor */
	Field newDescriptor = getDescriptorArray().insert(i);
	numberOfEntries++;

	/* create a new descriptor */
	newDescriptor.getSubfield(0, 2).put(entriesLength - usedSpaceMax);
	newDescriptor.getSubfield(2, 2).put(key.length);
	newDescriptor.getSubfield(4, 2).put(value.length);

	/* update the parent key for this node if this was the 0th entry */
	if (i == 0 && !parentAddress.isNull()) {
		IndexNode parent = acquireNode(parentAddress);
		if (numberOfEntries == 1) {
			parent.insertKeyForChild(address, key);
		} else {
			parent.updateKeyForChild(getKey(1), address, key);
		}
		parent.release();
	}
	modified();
}
/**
 * Inserts a child address into a non-leaf node.  This may result in this node splitting.
 */
private void insertKeyForChild(ObjectAddress childAddress, byte[] key) throws IndexedStoreException {
	int i = findFirstEntryGT(key);
	insertEntryBefore(i, key, childAddress.toByteArray());
	if (i == 0 && !parentAddress.isNull()) {
		IndexNode parent = acquireNode(parentAddress);
		parent.updateKeyForChild(getKey(1), address, key);
		parent.release();
	}
}
boolean isInterior() {
	return (nodeType == InteriorNode);
}
boolean isLeaf() {
	return (nodeType == LeafNode);
}
/**
 * Returns true if the object is mapped directly onto a page.  This will be true only if the object is
 * guaranteed to never move on the page.
 */
protected boolean isPageMapped() {
	return true;
}
boolean isRoot() {
	return (nodeType == RootNode);
}
/**
 * Places the contents of the buffer into the fields.
 * Subclasses should implement and call super.
 */
protected void materialize() throws ObjectStoreException {
	super.materialize();
	anchorAddress = new ObjectAddress(anchorAddressField.get());
	parentAddress = new ObjectAddress(parentAddressField.get());
	nextAddress = new ObjectAddress(nextAddressField.get());
	previousAddress = new ObjectAddress(previousAddressField.get());
	nodeType = nodeTypeField.getInt();
	numberOfEntries = numberOfEntriesField.getInt();
	usedSpace = usedSpaceField.getInt();
	usedSpaceMax = usedSpaceMaxField.getInt();
}
/**
 * Initializer to register the factory for this type.
 */
protected static void registerFactory() {
	ObjectStore.registerFactory(Type, new IndexNodeFactory());
}
/**
 * Removes a cursor that is registered with this node.
 */
void removeCursor(IndexCursor cursor) {
	cursors.remove(cursor);
}
/**
 * Removes the descriptor and key/value pair at the entry number given.  This may
 * result in the node becoming empty.  The caller will need to take steps to plan for this.
 */
void removeEntry(int i) throws IndexedStoreException {

	/* remove the (key,value) entry */
	byte[] key = getKey(i);
	Field f = getKeyValueField(i);
	f.clear();
	usedSpace -= f.length();

	/* remove the descriptor */
	getDescriptorArray().remove(i);
	numberOfEntries--;

	/* if the 0th entry was removed, need to update the parent node with the new "low key" */
	if (i == 0 && !parentAddress.isNull()) {
		IndexNode parent = acquireNode(parentAddress);
		if (numberOfEntries > 0) {
			parent.updateKeyForChild(key, address, getKey(0));
		} else {
			parent.removeKeyForChild(address);
		}
		parent.release();
	}

	/* Notify any cursors and the anchor */
	Object[] cursorArray = cursors.toArray();
	for (int j = 0; j < cursorArray.length; j++) {
		IndexCursor cursor = (IndexCursor) cursorArray[j];
		cursor.entryRemoved(i);
	}
	IndexAnchor anchor = acquireAnchor(anchorAddress);
	anchor.entryRemoved(this);
	anchor.release();
	modified();
}
/**
 * Removes a child node address reference from a non-leaf node.
 */
private void removeKeyForChild(ObjectAddress childAddress) throws IndexedStoreException {
	Field childAddressField = new Field(childAddress);
	int i = 0;
	while (i < numberOfEntries) {
		if (getValueField(i).compareTo(childAddressField) == 0)
			break;
		i++;
	}
	if (i < numberOfEntries)
		removeEntry(i);
}
private void setAnchorAddress(ObjectAddress address) {
	anchorAddress = address;
	modified();
}
/**
 * Sets the fields definitions.  Done after the contents are set.
 */
protected void setFields() {
	super.setFields();
	anchorAddressField = contents.getSubfield(AnchorAddressOffset, AnchorAddressLength);
	parentAddressField = contents.getSubfield(ParentAddressOffset, ParentAddressLength);
	previousAddressField = contents.getSubfield(PreviousAddressOffset, PreviousAddressLength);
	nextAddressField = contents.getSubfield(NextAddressOffset, NextAddressLength);
	nodeTypeField = contents.getSubfield(NodeTypeOffset, NodeTypeLength);
	usedSpaceField = contents.getSubfield(UsedSpaceOffset, UsedSpaceLength);
	usedSpaceMaxField = contents.getSubfield(UsedSpaceMaxOffset, UsedSpaceMaxLength);
	numberOfEntriesField = contents.getSubfield(NumberOfEntriesOffset, NumberOfEntriesLength);
	entriesField = contents.getSubfield(EntriesOffset, EntriesLength);
}
private void setNextAddress(ObjectAddress address) {
	nextAddress = address;
	modified();
}
private void setNodeType(int nodeType) {
	this.nodeType = nodeType;
	modified();
}
private void setNumberOfEntries(int numberOfEntries) {
	this.numberOfEntries = numberOfEntries;
	modified();
}
private void setParentAddress(ObjectAddress address) {
	parentAddress = address;
	modified();
}
private void setPreviousAddress(ObjectAddress address) {
	previousAddress = address;
	modified();
}
private void setUsedSpace(int usedSpace) {
	this.usedSpace = usedSpace;
	modified();
}
private void setUsedSpaceMax(int usedSpaceMax) {
	this.usedSpaceMax = usedSpaceMax;
	modified();
}
/**
 * Splits an index node.  This split results in a new "low key" being placed in the parent.  This may
 * cause a parent node to split as well.  Splits eventually propagate to the root node, cause it 
 * to split and a new root node to be created.
 */
private ObjectAddress split() throws IndexedStoreException {

	/* Nodes can only be split if there are at least 2 entries */
	int n = numberOfEntries;
	if (n < 2) {
		throw new IndexedStoreException(IndexedStoreException.IndexNodeNotSplit);
	}

	/* 
	If this is a root node, need to make it an interior node and create a new parent root node.
	Also need to modify the index anchor to indicate the new root node, and place this node (the old root node) 
	into the new root node.  The new root node can always accept its first entry without splitting.
	*/
	if (isRoot()) {
		ObjectAddress newRootNodeAddress = insertObject(new IndexNode(anchorAddress));
		parentAddress = newRootNodeAddress;
		nodeType = InteriorNode;
		IndexNode newRootNode = acquireNode(newRootNodeAddress);
		newRootNode.insertKeyForChild(address, getLowKey());
		newRootNode.release();
		IndexAnchor anchor = acquireAnchor(anchorAddress);
		anchor.setRootNodeAddress(newRootNodeAddress);
		anchor.release();
	}

	/*
	Get a new node, fill it with half the entries from this node, then compress this node.  The
	new node is created with current parent. However, the node at the current parentAddress may
	split when the key is added to it for the new node.  Non-leaf nodes compensate for this
	by updating the newNode's parentAddress during the insertion.
	*/
	ObjectAddress newNodeAddress = insertObject(new IndexNode(anchorAddress, parentAddress));
	IndexNode newNode = acquireNode(newNodeAddress);
	Field f1 = entriesField;
	Field f2 = newNode.getEntriesField();
	int k = n / 2;
	newNode.setUsedSpace(copyEntries(f1, n - k, k, f2));
	newNode.setUsedSpaceMax(newNode.getUsedSpace());
	newNode.setNumberOfEntries(k);
	usedSpace = usedSpace - newNode.getUsedSpace();
	numberOfEntries = n - k;
	compress();

	/*
	If this was a leaf node, need to set the previous and next pointers of the this node, 
	the new node, and the old "next" node.
	*/
	if (isLeaf()) {
		newNode.setNodeType(LeafNode);
		newNode.setNextAddress(nextAddress);
		newNode.setPreviousAddress(address);
		if (!nextAddress.isNull()) {
			IndexNode nextNode = acquireNode(nextAddress);
			nextNode.setPreviousAddress(newNodeAddress);
			nextNode.release();
		}
		nextAddress = newNodeAddress;
	}

	/*
	If this is a non-leaf node, need to update the parent addresses of any child nodes
	of the new node.  k is the number of entries in the new node.
	*/
	if (!isLeaf()) {
		for (int i = 0; i < k; i++) {
			ObjectAddress childAddress = new ObjectAddress(newNode.getValue(i));
			IndexNode childNode = acquireNode(childAddress);
			childNode.setParentAddress(newNodeAddress);
			childNode.release();
		}
	}

	/*
	Need to insert the new node's low key and address into the parent.  This may
	result in the parent splitting and having to update the parent address of this node.
	*/
	IndexNode parentNode = acquireNode(parentAddress);
	parentNode.insertKeyForChild(newNodeAddress, newNode.getLowKey());
	parentNode.release();

	/* Clean up. */
	newNode.release();

	/* Notify any cursors and the anchor */
	Object[] cursorArray = cursors.toArray();
	for (int j = 0; j < cursorArray.length; j++) {
		IndexCursor cursor = (IndexCursor) cursorArray[j];
		cursor.nodeSplit();
	}
	modified();
	return newNodeAddress;
}
public String toString() {
	if (contents == null)
		return "EmptyNode";
	StringBuffer b = new StringBuffer();
	if (isLeaf())
		b.append("LeafNode");
	if (isRoot())
		b.append("RootNode");
	if (isInterior())
		b.append("InteriorNode");
	b.append("\n  Address = ");
	b.append(address);
	b.append("\n  AnchorAddress = ");
	b.append(anchorAddress);
	b.append("\n  ParentAddress = ");
	b.append(parentAddress);
	b.append("\n  PreviousAddress = ");
	b.append(previousAddress);
	b.append("\n  NextAddress = ");
	b.append(nextAddress);
	b.append("\n  NumberOfEntries = ");
	b.append(numberOfEntries);
	b.append("\n  UsedSpace = ");
	b.append(usedSpace);
	b.append("\n  UsedSpaceMax = ");
	b.append(usedSpaceMax);
	return b.toString();
}
/**
 * Unlinks a node from its parent and siblings.  This does not modify the current node, but
 * does modify all the nodes and anchors pointing to it.  
 */
void unlink() throws IndexedStoreException {
	if (isRoot()) {
		IndexAnchor anchor = acquireAnchor(anchorAddress);
		anchor.setRootNodeAddress(ObjectAddress.Null);
		anchor.release();
	}
	if (!parentAddress.isNull()) {
		IndexNode parent = acquireNode(parentAddress);
		parent.removeKeyForChild(address);
		parent.release();
	}
	if (!nextAddress.isNull()) {
		IndexNode next = acquireNode(nextAddress);
		next.setPreviousAddress(previousAddress);
		next.release();
	}
	if (!previousAddress.isNull()) {
		IndexNode previous = acquireNode(previousAddress);
		previous.setNextAddress(nextAddress);
		previous.release();
	}
}
/**
 * Update the key and value at this entry to a new key and value.  This may result in a node split.
 * The caller must be able to recognize that the node has split and compensate for that.
 */
private void updateEntry(int i, byte[] key, byte[] value) throws IndexedStoreException {

	/*
	If the node needs to be split, split it and then attempt the update again.  Note that if
	this is a non-leaf node the value is a child address.  Unlike the insert of a key/value
	pair, the child is already in the node, thus a split will update its parent address properly
	and there is no need to handle that special case.
	*/
	Field entries = entriesField;
	int entriesLength = entries.length();
	int newKeyValueLength = key.length + value.length;
	int oldKeyValueLength = getKeyValueField(i).length();
	int neededSpace = newKeyValueLength - oldKeyValueLength;
	int freeSpace = entriesLength - ((numberOfEntries * DescriptorLength) + usedSpace);
	if (freeSpace < neededSpace) {
		ObjectAddress newNodeAddress = split();
		if (i >= numberOfEntries) {
			IndexNode newNode = acquireNode(newNodeAddress);
			newNode.updateEntry(i - getNumberOfEntries(), key, value);
			newNode.release();
		} else {
			updateEntry(i, key, value);
		}
		return;
	}

	/*
	The node has enough free space to do the update.
	Remove the old value and key fields from the space.
	We can do this just by modifying the descriptor.
	*/
	Field descriptor = getDescriptor(i);
	descriptor.getSubfield(0, 2).put(0);
	descriptor.getSubfield(4, 2).put(0);
	usedSpace -= oldKeyValueLength;
	compress(newKeyValueLength);

	/* place the value and key fields into the space */
	Pointer p = entries.pointTo(entriesLength - usedSpaceMax);
	p.dec(value.length).put(value);
	p.dec(key.length).put(key);
	usedSpaceMax += newKeyValueLength;
	usedSpace += newKeyValueLength;

	/* update the descriptor */
	descriptor.getSubfield(0, 2).put(entriesLength - usedSpaceMax);
	descriptor.getSubfield(2, 2).put(key.length);
	descriptor.getSubfield(4, 2).put(value.length);
	modified();
}
/**
 * Sets the key at this entry to a new key.  This may result in a node split.
 * The caller must be able to recognize that the node has split and compensate for that if necessary.
 */
private void updateKeyAt(int i, byte[] key) throws IndexedStoreException {
	updateEntry(i, key, getValue(i));
}
/**
 * Updates the key of an (key,address) entry in a non-leaf node.  The key must still be in order with respect
 * to the other keys of the node.
 */
private void updateKeyForChild(byte[] key, ObjectAddress childAddress, byte[] newKey) throws IndexedStoreException {
	Field childAddressField = new Field(childAddress.toByteArray());
	int i = findLastEntryLT(key) + 1;
	while (i < numberOfEntries) {
		if (getValueField(i).compareTo(childAddressField) == 0)
			break;
		i++;
	}
	if (i < numberOfEntries) {
		updateKeyAt(i, newKey);
		if (i == 0 && !parentAddress.isNull()) {
			IndexNode parent = acquireNode(parentAddress);
			parent.updateKeyForChild(key, address, newKey);
			parent.release();
		}
	}
}
/**
 * Sets the value at this entry to a new value.  This may result in a node split.
 * The caller must be able to recognize that the node has split and compensate for that.
 */
void updateValueAt(int i, byte[] value) throws IndexedStoreException {
	updateEntry(i, getKey(i), value);
}
}
