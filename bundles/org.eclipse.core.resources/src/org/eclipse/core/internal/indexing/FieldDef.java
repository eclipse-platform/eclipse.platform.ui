package org.eclipse.core.internal.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public class FieldDef {

	public static final int F_BYTE    = 1;
	public static final int F_INT     = 2;
	public static final int F_UINT    = 3;
	public static final int F_LONG    = 4;
	public static final int F_BYTES   = 5;

	public int offset;
	public int length;
	public int type;
	
	public FieldDef(int type, int offset, int length) {
		this.type = type;
		this.offset = offset;
		this.length = length;
	}

}
