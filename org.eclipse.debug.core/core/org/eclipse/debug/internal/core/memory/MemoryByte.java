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
package org.eclipse.debug.internal.core.memory;


/**
 * Represents a byte in the meomry block.
 * MemoryByte allows debug adapters to specify attributes for each of the 
 * individual byte from the memory block
 * @since 3.0
 */
public abstract class MemoryByte {
	
	public static final byte	READONLY	= 0x01;			// Attribute to indicate the the byte is read-only
	public static final byte	VALID		= 0x02;			// Attribute to indicate that the byte is valid
	public static final byte	CHANGED		= 0x04; 		// Attribute to indicate that the byte has changed
	public static final byte	UNCHANGED	= 0x08;			// Attribute to indicate that the byte is unchanged
															// The changed and unchanged attribute will only
															// take effect if IMemoryBlockExtension.supportsChangeManagement()
															// returns true.

	// value of the byte
	public byte value;
	
	// Flags for specifying the attributes
	// To specify VALID:  flags |= MemoryByte.VALID;
	// To specify READONLY:  flags |= MemoryByte.READONLY;
	public byte flags;
}
