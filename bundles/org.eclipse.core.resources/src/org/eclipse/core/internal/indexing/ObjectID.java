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

public class ObjectID implements Insertable {

	private static final int Size = 8;
	private static final int ObjectNumberOffset = 0;
	private long objectNumber;

	public ObjectID(byte[] b) throws IndexedStoreException {
		if (b.length != Size) {
			throw new IndexedStoreException(IndexedStoreException.ObjectIDInvalid);
		}
		Buffer buf = new Buffer(b);
		objectNumber = buf.getLong(ObjectNumberOffset, 8);
	}

	public ObjectID(long objectNumber) {
		this.objectNumber = objectNumber;
	}

	public boolean equals(Object anObject) {
		if (!(anObject instanceof ObjectID)) return false;
		ObjectID id = (ObjectID) anObject;
		if (this.objectNumber != id.objectNumber) return false;
		return true;
	}

	public int hashCode() {
		return (int)objectNumber;
	}

	public byte[] toByteArray() {
		Buffer buf = new Buffer(Size);
		buf.put(ObjectNumberOffset, 8, objectNumber);
		return buf.get();
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("id("); //$NON-NLS-1$
		b.append(objectNumber);
		b.append(")"); //$NON-NLS-1$
		return b.toString();
	}
}
