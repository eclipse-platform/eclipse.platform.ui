package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class IndexedStoreContext extends IndexedStoreObject {

	private static final int Size = 32;
	private static final int Type = 2;

	private static final int OpenNumberOffset = 2;
	private static final int OpenNumberLength = 4;
	private Field openNumberField;
	private int openNumber;
	
	private static final int ObjectDirectoryAddressOffset	= 6;
	private static final int ObjectDirectoryAddressLength = 4;
	private Field objectDirectoryAddressField;
	private ObjectAddress objectDirectoryAddress;
	
	private static final int IndexDirectoryAddressOffset = 10;
	private static final int IndexDirectoryAddressLength = 4;
	private Field indexDirectoryAddressField;
	private ObjectAddress indexDirectoryAddress;

/** 
 * Constructs a new context.
 */
IndexedStoreContext() {
	setContents(new Field(getMinimumSize()));
	type = getRequiredType();
	indexDirectoryAddress = ObjectAddress.Null;
	objectDirectoryAddress = ObjectAddress.Null;
	openNumber = 0;
}
/** 
 * Constructs a context from a field read from the store.
 */
IndexedStoreContext(Field f) throws ObjectStoreException {
	super(f);
}
/**
 * Places the contents of the fields into the buffer.
 * Subclasses should implement and call super.
 */
protected void dematerialize() {
	openNumberField.put(openNumber);
	objectDirectoryAddressField.put(objectDirectoryAddress);
	indexDirectoryAddressField.put(indexDirectoryAddress);
	super.dematerialize();
}
/**
 * Returns the index directory address from the buffer.
 */
ObjectAddress getIndexDirectoryAddress() {
	return indexDirectoryAddress;
}
/**
 * Returns the minimum size of this object's instance -- including its type field.
 * Subclasses should override.
 */
protected int getMinimumSize() {
	return Size;
}
/**
 * Returns the object directory address from the buffer.
 */
ObjectAddress getObjectDirectoryAddress() {
	return objectDirectoryAddress;
}
/**
 * Returns the number of times this store has been opened and modified.
 */
int getOpenNumber() {
	return openNumber;
}
/**
 * Returns the required type of this class of object.
 * Subclasses must override.
 */
protected int getRequiredType() {
	return Type;
}
/**
 * Increments the open number in the buffer.
 */
void incrementOpenNumber() {
	openNumber++;
	modified();
}
/**
 * Places the contents of the buffer into the fields.
 * Subclasses should implement and call super.
 */
protected void materialize() throws ObjectStoreException {
	super.materialize();
	openNumber = openNumberField.getInt();
	objectDirectoryAddress = new ObjectAddress(objectDirectoryAddressField.get());
	indexDirectoryAddress = new ObjectAddress(indexDirectoryAddressField.get());
}
/**
 * Registers the factory for this type.
 */
protected static void registerFactory() {
	ObjectStore.registerFactory(Type, new IndexedStoreContextFactory());
}
/**
 * Sets the fields definitions.  Done after the contents are set.
 */
protected void setFields() {
	super.setFields();
	openNumberField = contents.getSubfield(OpenNumberOffset, OpenNumberLength);
	objectDirectoryAddressField = contents.getSubfield(ObjectDirectoryAddressOffset, ObjectDirectoryAddressLength);
	indexDirectoryAddressField = contents.getSubfield(IndexDirectoryAddressOffset, IndexDirectoryAddressLength);
}
/**
 * Sets the index directory address.
 */
void setIndexDirectoryAddress(ObjectAddress address) {
	this.indexDirectoryAddress = address;
	modified();
}
/**
 * Sets the object directory address.
 */
void setObjectDirectoryAddress(ObjectAddress address) {
	this.objectDirectoryAddress = address;
	modified();
}
/**
 * Provides a printable representation of this object.
 */
public String toString() {
	StringBuffer b = new StringBuffer();
	b.append("Context(");
	if (contents != null) {
		b.append(getOpenNumber());
		b.append(",");
		b.append(getIndexDirectoryAddress());
		b.append(",");
		b.append(getObjectDirectoryAddress());
	}
	b.append(")");
	return b.toString();
}
}
