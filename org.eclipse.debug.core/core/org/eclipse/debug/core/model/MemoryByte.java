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
 * A byte of memory in a meomry block. Each byte of memory has a value and a set
 * of attributes indicating if the byte is read-only, valid, or if the value has
 * changed.
 * <p>
 * Clients may subclass this class. 
 * </p>
 * @since 3.1
 */
public abstract class MemoryByte {
	
    /**
     * Bit mask used in flags to indicate a byte is read-only.
     */
	public static final byte	READONLY	= 0x01;
	
	/**
	 * Bit mask used in flags to indicate a byte is valid.
	 * 
	 * TODO: specify what makes a byte valid - when is a byte invalid?
	 */
	public static final byte	VALID		= 0x02;
	
	/**
	 * Bit mask used in flags to indicate a byte has changed.
	 * 
	 * TODO: specify CHANGE flag more clearly - changed since when? 
	 */
	public static final byte	CHANGED		= 0x04;
	
	/**
	 * Bit mask used in flags to indicate a byte has no history to
	 * determine if the byte is changed or not. The change bit has
	 * no meaning if this bit is on.
	 */
	public static final byte 	UNKNOWN		= 0x08;
	
	/**
	 * Value of this byte.
	 */
	protected byte value;
	
	// Flags for specifying the attributes
	// To specify VALID:  flags |= MemoryByte.VALID;
	// To specify READONLY:  flags |= MemoryByte.READONLY;
	protected byte flags;
	
	/**
	 * No argument constructor
	 */
	public MemoryByte()
	{
	}
	
	/**
	 * @param byteValue - value of the byte
	 */
	public MemoryByte(byte byteValue)
	{
		value = byteValue;
	}
	
	/**
	 * @param byteValue - value of the byte
	 * @param byteFlags - attribute of the byte
	 */
	public MemoryByte(byte byteValue, byte byteFlags)
	{
		value = byteValue;
		flags = byteFlags;
	}


	/**
	 * @return Returns the flags.
	 */
	public byte getFlags() {
		return flags;
	}
	/**
	 * @param flags The flags to set.
	 */
	public void setFlags(byte flags) {
		this.flags = flags;
	}

	/**
	 * @return Returns the value.
	 */
	public byte getValue() {
		return value;
	}
	/**
	 * @param value The value to set.
	 */
	public void setValue(byte value) {
		this.value = value;
	}
	
	/**
	 * Set the byte as valid / invalid
	 * @param isValid
	 */
	public void setValid(boolean isValid)
	{
		flags |= MemoryByte.VALID;

		if (!isValid)
			flags ^= MemoryByte.VALID;
	}
	
	/**
	 * @return true if the byte is valid, false otherwise
	 */
	public boolean isValid()
	{
		return ((flags & MemoryByte.VALID) == MemoryByte.VALID);
	}
	
	/**
	 * Sets if the byte is readonly
	 * @param isReadonly
	 */
	public void setReadonly(boolean isReadonly)
	{
		flags |= MemoryByte.READONLY;
		
		if (!isReadonly)
			flags ^= MemoryByte.READONLY;
	}
	
	/**
	 * @return true if the byte is read-only, false otherwise
	 */
	public boolean isReadonly()
	{
		return ((flags & MemoryByte.READONLY) == MemoryByte.READONLY);
	}
	
	/**
	 * Sets the byte as changed or unchanged
	 * @param isChanged
	 */
	public void setChanged(boolean isChanged)
	{
		flags |= MemoryByte.CHANGED;
		
		if (!isChanged)
			flags ^= MemoryByte.CHANGED;
	}
	
	/**
	 * @return true if teh byte is changed, false otherwise
	 */
	public boolean isChanged()
	{
		return ((flags & MemoryByte.CHANGED) == MemoryByte.CHANGED);
	}
	
	/**
	 * Sets the byte as known/unknown for its change state. Change bit
	 * has no effect if this attribute is set to true.
	 * @param isUnknown
	 */
	public void setUnknown(boolean isUnknown)
	{
		flags |= MemoryByte.UNKNOWN;
		
		if (!isUnknown)
			flags ^= MemoryByte.UNKNOWN;
	}
	
	public boolean isUnknown()
	{
		return ((flags & MemoryByte.UNKNOWN) == MemoryByte.UNKNOWN);
	}
	
	
}
