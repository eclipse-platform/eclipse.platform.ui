/**********************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.indexing;
import org.eclipse.core.internal.indexing.*;

class TestObject extends StoredObject {

	private static final int TYPE = 99;
	protected byte[] value;
	
	/**
	 * Standard constructor -- constructs an object that will be inserted into a store
	 */
	TestObject(byte[] value) {
		super();
		this.value = new Field(value).get();
	}

	/**
	 * Standard constructor -- constructs an object from bytes that came from the store.
	 */
	TestObject(Field buffer, ObjectStore store, ObjectAddress address) throws ObjectStoreException {
		super(buffer, store, address);
	}

	/**
	 * Places the contents of the fields into the buffer.
	 * Subclasses should implement and call super.
	 */
	protected void insertValues(Field buffer) {
		super.insertValues(buffer);
		buffer.subfield(2).put(value);
	}

	/**
	 * Places the contents of the buffer into the fields.
	 * Subclasses should implement and call super.
	 */
	protected void extractValues(Field buffer) throws ObjectStoreException {
		super.extractValues(buffer);
		value = buffer.subfield(2).get();
	}

	/**
	 * Returns the maximum size of this object's instance -- including its type field.
	 * Subclasses should override.
	 */
	protected int getMaximumSize() {
		return ObjectStore.MAXIMUM_OBJECT_SIZE;
	}

	/**
	 * Returns the minimum size of this object's instance -- including its type field.
	 * Subclasses should override.
	 */
	protected int getMinimumSize() {
		return 2;
	}

	/**
	 * Returns the length of the stored object for this instance.  Including the
	 * type field.
	 */
	protected int length() {
		return 2 + value.length;
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
	 * Updates the value.
	 */
	protected void updateValue(byte[] bytes) {
		new Field(value).put(bytes);
		setChanged();
	}

	/**
	 * Returns a id tag to be used in toString
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("TestObject(");
		for (int i = 0; i < value.length; i++) {
			if (i > 0) buf.append(" ");
			if (i > 30) {
				buf.append("...");
				break;
			}
			buf.append(value[i]);
		}
		buf.append(")");
		return buf.toString();
	}
}
