package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class ObjectAddress implements Insertable {

	public static final int Size = 4;
	public static ObjectAddress Null = new ObjectAddress(0, 0);
	private static final int PageNumberOffset = 0;
	private static final int ObjectNumberOffset = 3;
	private int pageNumber;
	private int objectNumber;

	/**
	 * Constructor for an address from a four byte field.
	 */
	public ObjectAddress(byte[] b) throws IllegalArgumentException {
		if (b.length != Size) throw new IllegalArgumentException();
		Buffer buf = new Buffer(b);
		pageNumber = buf.getUInt(PageNumberOffset, 3);
		objectNumber = buf.getUInt(ObjectNumberOffset, 1);
	}
	/**
	 * Constructs an address from its constituent page and object numbers.
	 */
	public ObjectAddress(int pageNumber, int objectNumber) throws IllegalArgumentException {
		if (pageNumber == 0 && objectNumber == 0) {
			this.pageNumber = 0;
			this.objectNumber = 0;
			return;
		}
		if (pageNumber < 0 || pageNumber > 0xFFFFFF) throw new IllegalArgumentException();
		if (pageNumber % ObjectStorePage.SIZE == 0) throw new IllegalArgumentException();
		if (objectNumber < 0 || objectNumber > 0xFF) throw new IllegalArgumentException();
		this.pageNumber = pageNumber;
		this.objectNumber = objectNumber;
	}
	/**
	 * Returns true if and only if the addresses are equal.
	 */
	public boolean equals(Object anObject) {
		if (!(anObject instanceof ObjectAddress)) return false;
		ObjectAddress address = (ObjectAddress) anObject;
		if (pageNumber != address.pageNumber) return false;
		if (objectNumber != address.objectNumber) return false;
		return true;
	}
	/**
	 * Returns the object number from the address.
	 */
	public int getObjectNumber() {
		return objectNumber;
	}
	/** 
	 * Returns the page number from the address.
	 */
	public int getPageNumber() {
		return pageNumber;
	}
	/**
	 * Returns an int representing the hash code for the address.
	 */
	public int hashCode() {
		return (pageNumber << 8) | objectNumber;
	}
	/**
	 * Tests the address for the null address value.
	 */
	public boolean isNull() {
		return (pageNumber == 0 && objectNumber == 0);
	}
	/**
	 * Returns a byte array form of the address.
	 */
	public byte[] toByteArray() {
		Buffer buf = new Buffer(Size);
		buf.put(PageNumberOffset, 3, pageNumber);
		buf.put(ObjectNumberOffset, 1, objectNumber);
		return buf.get();
	}
	/**
	 * Returns a string representation of the address suitable for printing.
	 */
	public String toString() {
		StringBuffer b = new StringBuffer(10);
		b.append("(");
		b.append(getPageNumber());
		b.append(",");
		b.append(getObjectNumber());
		b.append(")");
		return b.toString();
	}
}
