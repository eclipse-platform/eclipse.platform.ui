package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class IndexedStoreContext extends IndexedStoreObject {

	public static final int SIZE = 32;
	public static final int TYPE = 2;

	/* 
	The open number field is no longer used to generate object ids, but may not be deleted since a non-zero 
	open number indicates that the object number field has not been initialized.
	*/
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

	private static final int ObjectNumberOffset = 14;
	private static final int ObjectNumberLength = 8;
	private Field objectNumberField;
	private long objectNumber;

	/** 
	 * Constructs a new context.
	 */
	IndexedStoreContext() {
		super();
		indexDirectoryAddress = ObjectAddress.Null;
		objectDirectoryAddress = ObjectAddress.Null;
		openNumber = 0;
		objectNumber = 0;
	}
	
	/** 
	 * Constructs a context from a field read from the store.
	 */
	IndexedStoreContext(Field f, ObjectStore store, ObjectAddress address) throws ObjectStoreException {
		super(f, store, address);
	}

	/**
	 * Sets the fields definitions as subfields of a contents field.
	 */
	protected void setFields(Field contents) {
		openNumberField = contents.subfield(OpenNumberOffset, OpenNumberLength);
		objectDirectoryAddressField = contents.subfield(ObjectDirectoryAddressOffset, ObjectDirectoryAddressLength);
		indexDirectoryAddressField = contents.subfield(IndexDirectoryAddressOffset, IndexDirectoryAddressLength);
		objectNumberField = contents.subfield(ObjectNumberOffset, ObjectNumberLength);
	}

	/**
	 * Places the contents of the buffer into the fields.
	 * Subclasses should implement and call super.
	 */
	protected void extractValues(Field contents) throws ObjectStoreException {
		super.extractValues(contents);
		setFields(contents);
		openNumber = openNumberField.getInt();
		objectDirectoryAddress = new ObjectAddress(objectDirectoryAddressField.get());
		indexDirectoryAddress = new ObjectAddress(indexDirectoryAddressField.get());
		objectNumber = objectNumberField.getLong();
		/* here is where we transition to using object numbers -- upward compatible change */
		if (openNumber > 0) {
			objectNumber = (long)openNumber << 32;
			openNumber = 0;
			setChanged();
		}
	}

	/**
	 * Places the contents of the fields into the buffer.
	 * Subclasses should implement and call super.
	 */
	protected void insertValues(Field contents) {
		super.insertValues(contents);
		setFields(contents);
		openNumberField.put(openNumber);
		objectDirectoryAddressField.put(objectDirectoryAddress);
		indexDirectoryAddressField.put(indexDirectoryAddress);
		objectNumberField.put(objectNumber);
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
		return SIZE;
	}

	/**
	 * Returns the object directory address from the buffer.
	 */
	ObjectAddress getObjectDirectoryAddress() {
		return objectDirectoryAddress;
	}

	/**
	 * Returns the required type of this class of object.
	 * Subclasses must override.
	 */
	protected int getRequiredType() {
		return TYPE;
	}

	/**
	 * Generates and returns the next object number.  This is essentially the
	 * count of the number of user-defined objects generated in this store.
	 */
	long getNextObjectNumber() {
		objectNumber++;
		setChanged();
		return objectNumber;
	}

	/**
	 * Sets the index directory address.
	 */
	void setIndexDirectoryAddress(ObjectAddress address) {
		this.indexDirectoryAddress = address;
		setChanged();
	}

	/**
	 * Sets the object directory address.
	 */
	void setObjectDirectoryAddress(ObjectAddress address) {
		this.objectDirectoryAddress = address;
		setChanged();
	}
	
	/**
	 * Provides a printable representation of this object.
	 */
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Context(");
		b.append(objectNumber);
		b.append(",");
		b.append(indexDirectoryAddress);
		b.append(",");
		b.append(objectDirectoryAddress);
		b.append(")");
		return b.toString();
	}

}
