package org.eclipse.debug.core.model;

/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
 
/**
 * Storage represents a block of memory in an execution context.
 * A block of storage is represented by a starting memory address
 * and a length. Not all debug architectures support the retrieval
 * of storage blocks.
 * 
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public interface IStorage extends IDebugElement {
	
	/**
	 * Returns the start address of this storage block.
	 * 
	 * @return the start address of this storage block
	 */
	public long getStartAddress();
	
	/**
	 * Returns the length of this storage block in bytes.
	 * 
	 * @return the length of this storage block in bytes
	 */	
	public long getLength();
	
	/**
	 * Returns the values of the bytes currently contained
	 * in this this storage block.
	 * 
	 * @return the values of the bytes currently contained
	 *  in this this storage block
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * </ul>
	 */	
	public byte[] getBytes() throws DebugException;
	
	/**
	 * Returns whether this storage block supports value modification
	 * 
	 * @return whether this storage block supports value modification
	 */
	public boolean supportsValueModification();
	
	/**
	 * Sets the value of a byte in this storage block at the specified
	 * offset within this storage block to the spcified value.
	 * Offsets are zero based.
	 * 
	 * @param offset the offset at which to set the new value
	 * @param value the new value
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This storage block does not support value modification</li>
	 * <li>The specified offset is greater than or equal to the length
	 *   of this storage block (index of out of range)</li>
	 * </ul>
	 */
	public void setValue(long offset, byte value) throws DebugException;
	
	/**
	 * Sets the value of this storage block to the spcified bytes.
	 * 
	 * @param bytes the new bytes for this storage block
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This storage block does not support value modification</li>
	 * <li>The length of the specified bytes is not the same as the length
	 *   of this stoarge block.</li>
	 * </ul>
	 */
	public void setValue(byte[] bytes) throws DebugException;
}

