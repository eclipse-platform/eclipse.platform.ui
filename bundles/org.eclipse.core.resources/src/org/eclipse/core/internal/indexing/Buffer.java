package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;

/**
 * Implements a storage area that is accessible at various offsets and lengths.
 */
public class Buffer {
	protected byte[] contents;
	private static final byte[] ZEROES = new byte[1024];

	/**
	 * Default constructor.
	 */
	protected Buffer() {
		super();
	}

	/**
	 * Create a new buffer using the given byte array as contents.  Note that this has
	 * the potential for aliasing side effects.
	 */
	public Buffer(byte[] contents) {
		this.contents = contents;
	}

	/**
	 * Create a new buffer of size n.
	 */
	public Buffer(int n) {
		contents = new byte[n];
	}

	/**
	 * Constructor for a new Buffer from an Insertable.
	 */
	public Buffer(Insertable anObject) {
		this.contents = anObject.toByteArray();
	}

	public void clear() {
		clear(contents, 0, contents.length);
	}

	private static void clear(byte[] buffer, int offset, int length) {
		int n = length;
		int p = offset;
		while (n > 0) {
			int m = Math.min(ZEROES.length, n);
			System.arraycopy(ZEROES, 0, buffer, p, m);
			p += m;
			n -= m;
		}
	}

	private static void clear(byte[] buffer, int offset, int length, byte value) {
		for (int i = offset; i < offset + length; i++) {
			buffer[i] = value;
		}
	}

	public void clear(int offset, int length) {
		clear(contents, offset, length);
	}

	public void clear(int offset, int length, byte value) {
		clear(contents, offset, length, value);
	}

	private static int compare(byte[] buffer1, int offset1, int length1, byte[] buffer2, int offset2, int length2) {
		if (length1 < length2) {
			return -compare(buffer2, offset2, length2, buffer1, offset1, length1);
		}
		for (int i = 0; i < length2; i++) {
			int j1 = buffer1[offset1 + i] & 255;
			int j2 = buffer2[offset2 + i] & 255;
			if (j1 > j2)
				return 1;
			if (j1 < j2)
				return -1;
		}
		if (length1 > length2)
			return 1;
		return 0;
	}

	public static int compare(Buffer buffer1, int offset1, int length1, Buffer buffer2, int offset2, int length2) {
		return compare(buffer1.contents, offset1, length1, buffer2.contents, offset2, length2);
	}

	public static int compare(Buffer buffer1, Buffer buffer2) {
		return compare(buffer1.contents, 0, buffer1.contents.length, buffer2.contents, 0, buffer2.contents.length);
	}

	public void copyInternal(int fromOffset, int toOffset, int length) {
		System.arraycopy(contents, fromOffset, contents, toOffset, length);
	}

	public void copyTo(byte[] buffer) {
		int n = Math.min(buffer.length, contents.length);
		System.arraycopy(contents, 0, buffer, 0, n);
	}

	public void copyFrom(byte[] buffer) {
		int n = Math.min(buffer.length, contents.length);
		System.arraycopy(buffer, 0, contents, 0, n);
	}

	public byte[] get() {
		return get(0, contents.length);
	}

	public byte[] get(int offset, int length) {
		byte[] result = new byte[length];
		System.arraycopy(contents, offset, result, 0, length);
		return result;
	}
	
	public byte[] get(FieldDef d) {
		return get(d.offset, d.length);
	}
	
	public Field getField(int offset, int length) {
		return new Field(this, offset, length);
		}
		
	public Field getField(FieldDef d) {
		return new Field(this, d.offset, d.length);
	}
		
	public byte getByte(int offset) {
		return contents[offset];
	}
	
	public int getInt(int offset, int length) {
		return (int)getLong(offset, length);
	}
	
	public int getInt(FieldDef d) {
		return (int)getLong(d.offset, d.length);
	}
	
	public int getUInt(int offset, int length) {
		int shift = Math.max(0, 32 - (length * 8));
		int mask = (-1 >>> shift) & Integer.MAX_VALUE;
		return getInt(offset, length) & mask;
	}
	
	public int getUInt(FieldDef d) {
		return getUInt(d.offset, d.length);
	}
	
	public long getLong(int offset, int length) {
		if (length <= 0) return 0;
		long v = contents[offset];
		for (int i = offset + 1; i < offset + length; i++) {
			v = (v << 8) | (contents[i] & 255);
		}
		return v;
	}
	
	public long getLong(FieldDef d) {
		return getLong(d.offset, d.length);
	}
	
	public byte[] getByteArray() {
		return contents;
	}
	
	public int length() {
		return contents.length;
		}
	
	public Pointer pointTo(int offset) {
		return new Pointer(this, offset);
	}
	
	public void put(int offset, byte value) {
		contents[offset] = value;
	}
	
	public void put(int offset, byte[] source) {
		System.arraycopy(source, 0, contents, offset, source.length);
	}
	
	public void put(int offset, int length, byte[] source) {
		int n = Math.min(length, source.length);
		System.arraycopy(source, 0, contents, offset, n);
	}
	
	public void put(FieldDef d, byte[] source) {
		put(d.offset, d.length, source);
	}
	
	public void put(int offset, int length, long n) {
		long v = n;
		int i = offset + length;
		while (i > offset) {
			i--;
			contents[i] = (byte)v;
			v = (v >>> 8);
		}
	}
	
	public void put(FieldDef d, long n) {
		put(d.offset, d.length, n);
	}
	
	public void put(int offset, int length, int n) {
		put(offset, length, (long)n);
	}
	
	public void put(FieldDef d, int n) {
		put(d.offset, d.length, (long)n);
	}
	
	public void put(int offset, Insertable source) {
		put(offset, source.toByteArray());
	}

}
