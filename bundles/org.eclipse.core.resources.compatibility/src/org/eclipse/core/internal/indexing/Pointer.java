/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.indexing;

class Pointer {
	protected Buffer buffer; // contents
	protected int offset; // offset of the field within the buffer

	/**
	 * Constructor for a new Pointer.
	 */
	public Pointer(Buffer buffer, int offset) {
		this.buffer = buffer;
		this.offset = offset;
	}

	public Pointer dec(int n) {
		offset -= n;
		return this;
	}

	public FieldArray getArray(int length, int stride, int count) {
		return new FieldArray(buffer, offset, length, stride, count);
	}

	public Field getField(int offset, int length) {
		return new Field(buffer, this.offset + offset, length);
	}

	public Pointer inc(int n) {
		offset += n;
		return this;
	}

	public Pointer put(byte[] bytes) {
		buffer.put(offset, bytes);
		return this;
	}
}
