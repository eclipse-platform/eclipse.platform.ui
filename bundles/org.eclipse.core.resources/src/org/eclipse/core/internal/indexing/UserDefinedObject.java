package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

class UserDefinedObject extends IndexedStoreObject {
	private static final int Type = 5;
	private static final int ValueFieldOffset = 2;
	private Field valueField;
/**
 * Standard constructor -- constructs an object that will be inserted into a store
 */
UserDefinedObject(byte[] value) {
	setContents(new Field(value.length + ValueFieldOffset));
	type = getRequiredType();
	valueField.put(value);
}
/**
 * Standard constructor -- constructs an object from bytes that came from the store.
 */
UserDefinedObject(Field f) throws ObjectStoreException {
	super(f);
}
/**
 * Places the contents of the fields into the buffer.
 * Subclasses should implement and call super.
 * The value field is maintained in the contents directly and does not need
 * to be copied there by this method.
 */
protected void dematerialize() {
	super.dematerialize();
}
/**
 * Returns the maximum size of this object's instance -- including its type field.
 * Subclasses should override.
 */
protected int getMaximumSize() {
	return 6000 + ValueFieldOffset;
}
/**
 * Returns the minimum size of this object's instance -- including its type field.
 * Subclasses should override.
 */
protected int getMinimumSize() {
	return ValueFieldOffset;
}
/**
 * Returns the required type of this class of object.
 * Subclasses must override.
 */
protected int getRequiredType() {
	return Type;
}
/**
 * Returns the value of the object.
 */
byte[] getValue() {
	return valueField.get();
}
/**
 * Places the contents of the buffer into the fields.
 * Subclasses should implement and call super.
 * The value is maintain in the contents field and does not need to be copied to a
 * separate instance field of this object.
 */
protected void materialize() throws ObjectStoreException {
	super.materialize();
}
/**
 * Registers the factory for this type.
 */
static void registerFactory() {
	ObjectStore.registerFactory(Type, new UserDefinedObjectFactory());
}
/**
 * Sets the fields definitions.  Done after the contents are set.
 */
protected void setFields() {
	super.setFields();
	valueField = contents.getSubfield(ValueFieldOffset, contents.length() - ValueFieldOffset);
}
/**
 * Returns a id tag to be used in toString
 */
protected String tagName() {
	return "UDO";
}
/**
 * Updates the value.
 */
protected void updateValue(byte[] bytes) {
	valueField.put(bytes);
	modified();
}
}
