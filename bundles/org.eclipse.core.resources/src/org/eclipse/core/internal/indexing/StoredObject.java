package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

abstract public class StoredObject implements IReferable, Insertable {

	public static final int Type = 0;
	protected static final int TypeOffset = 0;
	protected static final int TypeLength = 2;
	protected ObjectStore store;
	protected ObjectAddress address;
	protected ObjectPage page;
	protected Field contents;
	protected int referenceCount;
	protected int type;
	protected Field typeField;

/** 
 * Constructs a new object so that it can be stored.
 */
protected StoredObject() {
	setContents(new Field(getMinimumSize()));
	type = getRequiredType();
}
/** 
 * Constructs a new instance from a field.
 */
protected StoredObject(Field f) throws ObjectStoreException {
	if (f.length() < getMinimumSize())
		throw new ObjectStoreException(ObjectStoreException.ObjectSizeFailure);
	if (f.length() > getMaximumSize())
		throw new ObjectStoreException(ObjectStoreException.ObjectSizeFailure);
	if (isPageMapped())
		setContents(f);
	else
		setContents(new Field(f.get()));
	materialize();
	if (type != getRequiredType())
		throw new ObjectStoreException(ObjectStoreException.ObjectTypeFailure);
}
/**
 * Notifies an object that it has been acquired.  Page Mapped objects need to acquire their page.
 */
final void acquired() throws ObjectStoreException {
	if (this.isPageMapped()) page = store.acquireObjectPage(address.getPageNumber());
}
/**
 * Adds a reference.
 */
public final int addReference() {
	referenceCount++;
	return referenceCount;
}
/**
 * Places the contents of the fields into the buffer.
 * Subclasses should implement and call super.
 */
protected void dematerialize() {
	typeField.put(type);
}
/**
 * Returns the address of the object.
 * Subclasses must not override.
 */
public final ObjectAddress getAddress() {
	return address;
}
/**
 * Returns the maximum size of this object's instance -- including its type field.
 * Subclasses can override.  The default is to have the this be equal to the minimum
 * size, forcing a fixed size object.
 */
protected int getMaximumSize() {
	return getMinimumSize();
}
/**
 * Returns the minimum size of this object's instance -- including its type field.
 * Subclasses should override.
 */
protected int getMinimumSize() {
	return 2;
}
	/**
	 * Returns the required type of this class of object.
	 * Subclasses must override.
	 */
	protected int getRequiredType() {
		return Type;
	}
/**
 * Returns the store of the object.
 * Subclasses must not override.
 */
final ObjectStore getStore() {
	return store;
}
/**
 * Tests for existing references.
 */
public final boolean hasReferences() {
	return referenceCount > 0;
}
/**
 * Returns true if the object is mapped directly onto a page.  This will be true only if the object is
 * guaranteed to never move on the page.
 */
boolean isPageMapped() {
	return false;
}
/**
 * Returns the size of this object's instance -- including its type field.
 */
public final int length() {
	return contents.length();
}
/**
 * Places the contents of the buffer into the fields.
 * Subclasses should implement and call super.
 */
protected void materialize() throws ObjectStoreException {
	type = typeField.getInt();
}
/**
 * Called when a modification is detected.  Subclasses must not override.  Subclasses should call when
 * a change is detected that would cause this object to be stored.
 */
protected final void modified() {
	store.updateObject(this);
}
/**
 * Registers the factory for this type.  There is no factory for Stored Objects.  Concrete subclasses
 * must implement and define their own factories.
 */
static void registerFactory() {
}
/**
 * Notifies an object that it has been released.  Page Mapped objects need to release their page.
 */
final void released() throws ObjectStoreException {
	if (this.isPageMapped())
		store.releaseObjectPage(page);
}
/**
 * Removes a reference.
 */
public final int removeReference() {
	if (referenceCount > 0)
		referenceCount--;
	return referenceCount;
}
/**
 * Sets the address.  Used only at object creation time.
 */
final void setAddress(ObjectAddress address) {
	this.address = address;
}
/**
 * Sets the contents.  Fields on the contents are defined at this time.
 */
protected final void setContents(Field f) {
	this.contents = f;
	setFields();
}
/**
 * Sets the fields definitions.  Done after the contents are set.  Subclasses should implement
 * and call super.
 */
protected void setFields() {
	typeField = contents.getSubfield(TypeOffset, TypeLength);
}
/**
 * Sets the store.  Used only at object creation time.
 */
final void setStore(ObjectStore store) {
	this.store = store;
}
/**
 * Returns a tag used in toString.  Subclasses should implement.
 */
protected String tagName() {
	return "StoredObject";
}
/**
 * Returns a byte array value of the object.
 */
public final byte[] toByteArray() {
	dematerialize();
	return contents.get();
}
/** 
 * Provides a printable representation of this object.
 * Subclasses should override.
 */
public String toString() {
	StringBuffer b = new StringBuffer();
	b.append(tagName());
	b.append("(");
	int valueOffset = TypeOffset + TypeLength;
	int valueLength = contents.length() - valueOffset;
	Pointer p = contents.pointTo(valueOffset);
	b.append(valueLength);
	b.append(" [");
	int i = 0;
	while (true) {
		if (i > 0)
			b.append(" ");
		if (i == Math.min(10, valueLength))
			break;
		b.append(p.getUInt(1));
		p.inc(1);
		i++;
	}
	if (valueLength > 10)
		b.append(" ...");
	b.append("])");
	return b.toString();
}
}
