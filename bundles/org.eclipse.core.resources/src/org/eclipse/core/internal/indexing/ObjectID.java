package org.eclipse.core.internal.indexing;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

public class ObjectID implements Insertable {

	private static final int Size = 8;
	private static final int OpenNumberOffset = 0;
	private static final int SerialNumberOffset = 4;
	private int openNumber;
	private int serialNumber;

	public ObjectID(byte[] b) throws IndexedStoreException {
		if (b.length != Size) {
			throw new IndexedStoreException(IndexedStoreException.ObjectIDInvalid);
		}
		Buffer buf = new Buffer(b);
		openNumber = buf.getInt(OpenNumberOffset, 4);
		serialNumber = buf.getInt(SerialNumberOffset, 4);
	}
	public ObjectID(int openNumber, int serialNumber) {
		this.openNumber = openNumber;
		this.serialNumber = serialNumber;
	}
	public boolean equals(Object anObject) {
		if (!(anObject instanceof ObjectID)) return false;
		ObjectID id = (ObjectID) anObject;
		if (this.openNumber != id.openNumber) return false;
		if (this.serialNumber != id.serialNumber) return false;
		return true;
	}
	public int hashCode() {
		return (openNumber << 8) | serialNumber;
	}
	public byte[] toByteArray() {
		Buffer buf = new Buffer(Size);
		buf.put(OpenNumberOffset, 4, openNumber);
		buf.put(SerialNumberOffset, 4, serialNumber);
		return buf.get();
	}
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("id(");
		b.append(openNumber);
		b.append(",");
		b.append(serialNumber);
		b.append(")");
		return b.toString();
	}
}
