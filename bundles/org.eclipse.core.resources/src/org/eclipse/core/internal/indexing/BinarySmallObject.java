package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class BinarySmallObject extends IndexedStoreObject {
	public static final int TYPE = 5;
	public static final int VALUE_OFFSET = 2;
	protected byte[] value;
	
	/**
	 * Constructs a new object that will be inserted into a store.
	 */
	public BinarySmallObject(byte[] value) {
		super();
		this.value = new Buffer(value).get();
	}
	
	/**
	 * Constructs an object from bytes that came from the store.
	 */
	public BinarySmallObject(Field f, ObjectStore store, ObjectAddress address) throws ObjectStoreException {
		super(f, store, address);
	}
	
	/**
	 * Places the contents of the fields into the buffer.
	 * Subclasses should implement and call super.
	 * The value field is maintained in the contents directly and does not need
	 * to be copied there by this method.
	 */
	protected void insertValues(Field f) {
		super.insertValues(f);
		f.subfield(VALUE_OFFSET).put(value);
	}
	/**
	 * Extracts the values from a field into the members of this object;
	 */
	protected void extractValues(Field f) throws ObjectStoreException {
		super.extractValues(f);
		value = f.subfield(VALUE_OFFSET).get();
	}
	
	/**
	 * Returns the maximum size of this object's instance -- including its type field.
	 * Subclasses should override.
	 */
	protected int getMaximumSize() {
		return 6000 + VALUE_OFFSET;
	}
	
	protected int length() {
		return value.length + VALUE_OFFSET;
	}
	
	/**
	 * Returns the minimum size of this object's instance -- including its type field.
	 * Subclasses should override.
	 */
	protected int getMinimumSize() {
		return VALUE_OFFSET;
	}
	
	/**
	 * Returns the required type of this class of object.
	 * Subclasses must override.
	 */
	protected int getRequiredType() {
		return TYPE;
	}
	
	/**
	 * Returns the value of the object.
	 */
	public byte[] getValue() {
		return new Field(value).get();
	}
	
	/** 
	 * Provides a printable representation of this object.
	 */
	public String toString() {
		int n = 10;
		StringBuffer b = new StringBuffer();
		b.append("BSOB(");
		b.append(value.length);
		b.append(" [");
		for (int i = 0; i < value.length; i++) {
			if (i > 0) b.append(" ");
			if (i == n) break;
			b.append(value[i]);
		}
		if (value.length > n) b.append(" ...");
		b.append("])");
		return b.toString();
	}

}
