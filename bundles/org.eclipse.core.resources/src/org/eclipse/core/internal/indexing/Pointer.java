package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

class Pointer {
	protected Buffer buffer; // contents
	protected int offset; // offset of the field within the buffer
	
	/**
	 * Default constructor for a new Field -- do not use.
	 */
	protected Pointer() {
	}
	
	/**
	 * Constructor for a new Pointer.
	 */
	public Pointer(Buffer buffer, int offset) {
		this.buffer = buffer;
		this.offset = offset;
	}
	
	public void clear(int length) {
		buffer.clear(offset, length);
	}
	
	public Pointer dec(int n) {
		offset -= n;
		return this;
	}
		
	public byte[] get(int length) {
		return buffer.get(offset, length);
	}
		
	public FieldArray getArray(int length, int stride, int count) {
		return new FieldArray(buffer, offset, length, stride, count);
	}
	
	public Field getField(int length) {
		return new Field(buffer, offset, length);
	}
	
	public Field getField(int offset, int length) {
		return new Field(buffer, this.offset + offset, length);
	}
	
	public int getInt(int length) {
		return buffer.getInt(offset, length);
	}
	
	public int getUInt(int length) {
		return buffer.getUInt(offset, length);
	}
	
	public Pointer inc(int n) {
		offset += n;
		return this;
	}
	
	public int offset() {
		return offset;
	}
	
	public Pointer put(byte[] bytes) {
		buffer.put(offset, bytes);
		return this;
	}
	
	public Pointer put(int length, int n) {
		buffer.put(offset, length, n);
		return this;
	}
	
	public Pointer put(int length, long n) {
		buffer.put(offset, length, n);
		return this;
	}
	
	public Pointer put(Insertable anObject) {
		return put(anObject.toByteArray());
	}

}
