package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
 
/**
 * Some debug targets support the retrieval of arbitrary blocks of
 * memory.
 * 
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see IMemoryBlock
 */
public interface IMemoryBlockRetrieval {
	
	/**
	 * Returns whether this debug target supports the retreival
	 * of memory blocks.
	 * 
	 * @return whether this debug target supports the retreival
	 *  of memory blocks
	 */
	public boolean supportsStorageRetrieval();
	
	/**
	 * Returns a memory block that starts at the specified
	 * memory address, with the specified length.
	 * 
	 * @param startAddress starting address
	 * @param length length of the memory block in bytes
	 * @return a memory block that starts at the specified
	 *  memory address, with the specified length
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This debug target does not support memory block retreival</li>
	 * <li>The specified address and length are not within valid
	 *  ranges</li>
	 * </ul>
	 */
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException;
}

