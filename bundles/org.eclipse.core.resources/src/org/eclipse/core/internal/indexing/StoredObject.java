package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

public abstract class StoredObject extends Observable implements Referable, Insertable {

	public static final int MAXIMUM_OBJECT_SIZE = ObjectStore.MAXIMUM_OBJECT_SIZE;
	public static final int TYPE_OFFSET = 0;
	public static final int TYPE_LENGTH = 2;

	protected ObjectStore store;
	protected ObjectAddress address;
	protected int referenceCount;
	protected int type;

	/** 
	 * Constructs a new object so that it can be stored.
	 */
	protected StoredObject() {
		type = getRequiredType();
	}
	
	/** 
	 * Constructs a new instance from a field.
	 */
	protected StoredObject(Field f, ObjectStore store, ObjectAddress address) throws ObjectStoreException {
		if (f.length() < getMinimumSize()) {
			throw new ObjectStoreException(ObjectStoreException.ObjectSizeFailure);
		}
		if (f.length() > getMaximumSize()) {
			throw new ObjectStoreException(ObjectStoreException.ObjectSizeFailure);
		}
		extractValues(f);
		setStore(store);
		setAddress(address);
	}

	/** 
	 * Provides a printable representation of this object.  Subclasses must implement.
	 */
	public abstract String toString();

	/**
	 * Returns the required type of this class of object.
	 * Subclasses must override.
	 */
	protected abstract int getRequiredType();

	/**
	 * Returns a byte array value of the object.
	 */
	public final byte[] toByteArray() {
		Field f = new Field(length());
		insertValues(f);
		return f.get();
	}

	/**
	 * Adds a reference.
	 */
	public final int addReference() {
		referenceCount++;
		return referenceCount;
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
	 * Tests for existing references.
	 */
	public final boolean hasReferences() {
		return referenceCount > 0;
	}

	/**
	 * Returns the store of the object.
	 * Subclasses must not override.
	 */
	public final ObjectStore getStore() {
		return store;
	}
	
	/**
	 * Returns the address of the object.
	 * Subclasses must not override.
	 */
	public final ObjectAddress getAddress() {
		return address;
	}

	public final void setStore(ObjectStore store) {
		this.store = store;
	}

	public final void setAddress(ObjectAddress address) {
		this.address = address;
	}
	
	/**
	 * Places the contents of the buffer into the members.
	 * Subclasses should implement and call super.
	 */
	protected void extractValues(Field f) throws ObjectStoreException {
		type = f.subfield(TYPE_OFFSET, TYPE_LENGTH).getInt();
		if (type != getRequiredType()) throw new ObjectStoreException(ObjectStoreException.ObjectTypeFailure);
	}	
	
	/**
	 * Places the contents of the fields into the buffer.
	 * Subclasses should implement and call super.
	 */
	protected void insertValues(Field f) {
		f.subfield(TYPE_OFFSET, TYPE_LENGTH).put(type);
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
	 * Returns the actual size of this object's instance -- including its type field.
	 * Subclasses should override.
	 */
	protected int length() {
		return getMinimumSize();
	}
}
