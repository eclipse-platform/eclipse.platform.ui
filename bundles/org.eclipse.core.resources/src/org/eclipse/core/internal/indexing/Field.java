package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;

public class Field implements Insertable {
	protected Buffer buffer; // contents
	protected int offset; // offset of the field within the buffer
	protected int length; // length of the field
	
/**
 * Default constructor for a new Field -- do not use.
 */
protected Field() {
}
/**
 * Constructor for a new Field.
 */
public Field(byte[] bytes) {
	this.buffer = new Buffer(bytes);
	this.offset = 0;
	this.length = bytes.length;
}
/**
 * Constructor for a new Field.
 */
public Field(int n) {
	this.buffer = new Buffer(n);
	this.offset = 0;
	this.length = n;
}
/**
 * Constructor for a new Field.
 */
public Field(Buffer buffer, int offset, int length) {
	this.buffer = buffer;
	this.offset = offset;
	this.length = length;
}
/**
 * Constructor for a new Field.
 */
public Field(Insertable anObject) {
	buffer = new Buffer(anObject);
	offset = 0;
	length = buffer.length();
}
public Field clear() {
	buffer.clear(offset, length);
	return this;
}
public Field clear(byte value) {
	buffer.clear(offset, length, value);
	return this;
}
public int compareTo(Field that) {
	return Buffer.compare(this.buffer, this.offset, this.length, that.buffer, that.offset, that.length);
}
public byte[] get() {
	return buffer.get(offset, length);
	}
public int getInt() {
	return buffer.getInt(offset, length);
}
public long getLong() {
	return buffer.getLong(offset, length);
}
public Field getSubfield(int offset, int length) {
	if (offset + length > this.length) throw new IllegalArgumentException();
	return buffer.getField(this.offset + offset, length);
}
public int getUInt() {
	return buffer.getUInt(offset, length);
}
public int length() {
	return length;
	}
public int offset() {
	return offset;
	}
public Pointer pointTo(int offset) {
	return new Pointer(buffer, this.offset + offset);
	}
public Field put(byte[] b) {
	buffer.put(offset, length, b);
	return this;
	}
public Field put(int n) {
	buffer.put(offset, length, n);
	return this;
}
public Field put(long n) {
	buffer.put(offset, length, n);
	return this;
}
public Field put(Insertable anObject) {
	return put(anObject.toByteArray());
	}
public Field readFrom(InputStream in) throws IOException {
	buffer.readFrom(in, offset, length);
	return this;
	}
/**
 * Implementation of the Insertable interface.
 */
public byte[] toByteArray() {
	return get();
}
public Field writeTo(OutputStream out) throws IOException {
	buffer.writeTo(out, offset, length);
	return this;
	}
}
