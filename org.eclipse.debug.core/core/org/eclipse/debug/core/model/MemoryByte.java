/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;


/**
 * A byte of memory in a meomry block. Each byte of memory has a value and
 * attributes indicating if the byte is read-only, valid, or if its value has
 * changed.
 * <p>
 * Clients may subclass this class.
 * TODO: when would a client subclass this class?
 * </p>
 * @since 3.1
 */
public abstract class MemoryByte {
	
    /**
     * Bit mask used to indicate a byte is read-only.
     */
	public static final byte	READONLY	= 0x01;
	
	/**
	 * Bit mask used to indicate a byte is valid.
	 * 
	 * TODO: specify what makes a byte valid - when is a byte invalid?
	 */
	public static final byte	VALID		= 0x02;
	
	/**
	 * Bit mask used to indicate a byte has changed.
	 * 
	 * TODO: specify CHANGE flag more clearly - changed since when? 
	 */
	public static final byte	CHANGED		= 0x04;
	
	/**
	 * Bit mask used to indicate a byte has no history to
	 * determine if the byte is changed or not. The change bit has
	 * no meaning if this bit is on.
	 */
	public static final byte 	UNKNOWN		= 0x08;
	
	/**
	 * Value of this byte.
	 */
	protected byte value;
	
	/**
	 * Attribute flags.
	 * <p>
	 * To specify VALID:  flags |= MemoryByte.VALID;
	 * To specify READONLY:  flags |= MemoryByte.READONLY;
	 * </p>
	 */
	protected byte flags;
	
	/**
	 * Constructs a read-write, invalid, unchanged memory byte with a
	 * value of 0.
	 * 
	 * TODO: should the valid flag be set for convenience?
	 */
	public MemoryByte() {
	}
	
	/**
	 * Constructs a read-write, invalid, unchanged memory byte with
	 * the given value.
	 * 
	 * @param byteValue value of this memory byte
	 * 
	 * TODO: should the valid flag be set for convenience?
	 */
	public MemoryByte(byte byteValue) {
		value = byteValue;
	}
	
	/**
	 * Constructs a memory byte with the given value and attributes.
	 * 
	 * @param byteValue value of this memory byte
	 * @param byteFlags attributes of the byte specified as a bit mask 
	 */
	public MemoryByte(byte byteValue, byte byteFlags) {
		value = byteValue;
		flags = byteFlags;
	}

	/**
	 * Returns this memory byte's attribute as a bit mask.
	 * 
	 * @return this memory byte's attribute as a bit mask
	 */
	public byte getFlags() {
		return flags;
	}
	/**
	 * Sets this memory byte's attributes based on the given bit mask.
	 * 
	 * @param flags bit mask of attributes
	 */
	public void setFlags(byte flags) {
		this.flags = flags;
	}

	/**
	 * Returns the value of this memory byte.
	 * 
	 * @return the value of this memory byte
	 */
	public byte getValue() {
		return value;
	}
	
	/**
	 * Sets the value of this memory byte.
	 * 
	 * @param value the new value of this memory byte
	 */
	public void setValue(byte value) {
		this.value = value;
	}
	
	/**
	 * Sets whether this memory byte is valid.
	 * 
	 * @param valid whether this memory byte is valid
	 */
	public void setValid(boolean valid) {
		flags |= MemoryByte.VALID;
		if (!valid)
			flags ^= MemoryByte.VALID;
	}
	
	/**
	 * Returns whether this memory byte is valid.
	 * 
	 * @return whether this memory byte is valid
	 */
	public boolean isValid() {
		return ((flags & MemoryByte.VALID) == MemoryByte.VALID);
	}
	
	/**
	 * Sets whether this memory byte is read-only.
	 * 
	 * @param readonly whether this memory byte is read-only.
	 */
	public void setReadonly(boolean readonly) {
		flags |= MemoryByte.READONLY;
		if (!readonly)
			flags ^= MemoryByte.READONLY;
	}
	
	/**
	 * Returns whether this memory byte is read-only.
	 * 
	 * @return whether this memory byte is read-only
	 */
	public boolean isReadonly() {
		return ((flags & MemoryByte.READONLY) == MemoryByte.READONLY);
	}
	
	/**
	 * Sets whether this memory byte has changed.
	 * 
	 * @param changed whether this memory byte has changed
	 */
	public void setChanged(boolean changed) {
		flags |= MemoryByte.CHANGED;
		if (!changed)
			flags ^= MemoryByte.CHANGED;
	}
	
	/**
	 * Returns whether this memory byte has changed.
	 * 
	 * @return whether this memory byte has changed
	 */
	public boolean isChanged() {
		return ((flags & MemoryByte.CHANGED) == MemoryByte.CHANGED);
	}
	
	/**
	 * Sets whether the value of this byte is unknown. When a value
	 * is unknown, the change state of a memory byte has no meaning.
	 * 
	 * TODO: this flag should be changed to the 'positive' KNOWN rather than
	 *  the 'negative' UNKNOWN. All other flags read in the positive state:
	 *  is valid, is read-only, is changed. Similarly, this flag should read
	 *  as is known.
	 * 
	 * @param unknown whether the value of this byte is unknown
	 */
	public void setUnknown(boolean unknown) {
		flags |= MemoryByte.UNKNOWN;
		if (!unknown)
			flags ^= MemoryByte.UNKNOWN;
	}
	
	/**
	 * Returns whether the value of this byte is unknown. When a value
	 * is unknown, the change state of a memory byte has no meaning.
	 * 
	 * @return whether the value of this byte is unknown
	 */
	public boolean isUnknown() {
		return ((flags & MemoryByte.UNKNOWN) == MemoryByte.UNKNOWN);
	}
	
	
}
