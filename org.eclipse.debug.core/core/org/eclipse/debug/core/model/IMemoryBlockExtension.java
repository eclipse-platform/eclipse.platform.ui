/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;

/**
 * Extensions to (@link org.eclipse.debug.core.model.IMemoryBlock}. Allows
 * for bytes to be accessed in a larger address space, and for state information
 * to be provided for each byte. 
 * <p>
 * Clients may optionally implement this interface when providing implementations of
 * {@link org.eclipse.debug.core.model.IMemoryBlock}.
 * </p>
 * @since 3.1
 * @see org.eclipse.debug.core.model.MemoryByte
 */
public interface IMemoryBlockExtension extends IMemoryBlock {
	
	/**
	 * Returns the expression used to create this memory block. An expression can
	 * be used as name for a memory block and typically represents an expression
	 * used to compute a base address for a memory block.
	 * 
	 * @return the expression used to create this memory block
	 */
	public String getExpression();	
	
	/**
	 * Returns the base address of this memory block as a big integer.
	 * 
	 * @return the base address of this memory block
	 * @throws DebugException if unable to retreive the base address
	 */
	public BigInteger getBigBaseAddress() throws DebugException;
	
	/**
	 * Returns the hard start address of this memory block as a big integer, or 
	 * <code>null</code> if none. A <code>null</code> start address indicates that
	 * memory may be retrieved outside the bounds of this memory block's base
	 * address and length.
	 * 
	 * @return the hard start address of this memory block or <code>null</code>
	 * @throws DebugException if unable to retrieve the start address of this memory block.
	 */
	public BigInteger getMemoryBlockStartAddress() throws DebugException;
	
	/**
	 * Returns the hard end address of this memory block as a big integer, or
	 * <code>null</code> if none. A <code>null</code> end address indicates that
	 * memory may be retrieved outside the bounds of this memory block's base
	 * address and length. 
	 * 
	 * @return the hard end address of this memory block or <code>null</code>
	 * @throws DebugException if unable to retrieve the end address of this memory block.
	 */
	public BigInteger getMemoryBlockEndAddress() throws DebugException;
	
	/**
	 * Returns the address size of this memory block in number of bytes. The address
	 * size indicates the number of bytes used to construct an address.
	 *  
	 * @return address size in number of bytes
	 * @throws DebugException if unable to retrieve the address size
	 */
	public int getAddressSize() throws DebugException;
	
	/**
	 * Returns whether the base address of this memory block can be modified.
	 * 
	 * @return whether the base address of this memory block can be modified
	 * @throws DebugException is unable to determine if base address modification is supported
	 */
	public boolean supportBaseAddressModification() throws DebugException;
	
	/**
	 * Returns whether this memory block manages the change state of its bytes.
	 * If a memory block manages changes, the memory block is responsible for
	 * setting the <code>CHANGED</code> state of its <code>MemoryByte</code>'s
	 * returned from <code>getBytesFromAddress</code> and
	 * <code>getBytesFromOffset</code>.
	 * 
	 * @return whether this memory block manages the change state of its bytes  
	 */
	public boolean supportsChangeManagement();
	
	/**
	 * Sets the base address of this memory block to the given address.
	 * 
	 * @param address new base address
	 * @throws DebugException if unable to modify the base address, or modification
	 *  of the base address fails
	 */
	public void setBaseAddress(BigInteger address) throws DebugException;

	/**
	 * Returns bytes from this memory block based on the base address and
	 * addressable size of this memory block.
	 * 
	 * TODO:  please review this statement, this is to tell clients that 
	 *  the offset can cross memory block boundary. 
	 *  
	 *  When asked to return bytes that are beyond the memory block's start
	 *  or end address and if the memory block is unable to retrieve that memory,
	 *  return the required memory bytes with their READABLE bit set to 0.
	 *  
	 * @param offset zero based offset into this memory block at which to start
	 *  retrieving bytes.  Client should retrieve memory starting from "base address + offset".
	 * @param units the number of addressible units to retrieve
	 * @return an array of bytes from this memory block based on the given offset
	 *  and number of units. The size of the array returned must to be equal to 
	 *  <code>units</code> * <code>getAddressableSize()</code>.
	 * @throws DebugException if unable to retrieve the specified bytes
	 * @see MemoryByte
	 */
	public MemoryByte[] getBytesFromOffset(BigInteger offset, long units) throws DebugException;
	
	/**
	 * Returns bytes from this memory block based on the given address and the
	 * addressable size of this memory block.
	 * 
	 *  TODO:  again, please review the following statement:
	 *  When asked to return bytes that are beyond the memory block's start
	 *  or end address and if the memory block is unable to retrieve that memory,
	 *  return the required memory bytes with their READABLE bit set to 0.
	 *   
	 * @param address address at which to begin retrieving bytes
	 * @param units is the number of addressible units of memory to retrieve 
	 * @return an array of bytes from this memory block based on the given address
	 *  and number of units. The size of the array returned must to be equal to 
	 *  <code>units</code> * <code>getAddressableSize()</code>.
	 * @throws DebugException if unable to retrieve the specified bytes
	 * @see MemoryByte
	 */
	public MemoryByte[] getBytesFromAddress(BigInteger address, long units) throws DebugException;	

	/**
	 * Sets bytes in this memory block at the specified offset within this memory block to
	 * the spcified bytes. The offset is zero based. After successfully modifying the
	 * specified bytes, a debug event should be fired with a kind of <code>CHANGE</code>. 
	 * 
	 * @param offset the zero based offset at which to set the new value.  Modify
	 * the memory starting from base address + offset.
	 * @param bytes replcement bytes
	 * @exception DebugException if this method fails.  Reasons include:
	 * <ul><li>Failure communicating with the debug target.  The DebugException's
	 * status code contains the underlying exception responsible for
	 * the failure.</li>
	 * <li>This memory block does not support value modification</li>
	 * <li>The specified offset is greater than or equal to the length
	 *   of this memory block, or the number of bytes specified goes
	 *   beyond the end of this memory block (index of out of range)</li>
	 * </ul>
	 * @see org.eclipse.debug.core.DebugEvent
	 */
	public void setValue(BigInteger offset, byte[] bytes) throws DebugException;

	/**
	 * Connects the given client to this memory block. Allows a memory block
	 * to know when it is being monitored. Has no effect if an identical
	 * client is already connected.
	 * <p>
	 * TODO: what is a memory block responsible for when there are connections?
	 * </p>
	 * @param client the client to connect
	 */
	public void connect(Object client);
	
	/**
	 * Disconnects the given client from this memory block. Has no effect if
	 * an identical client is not already connected.
	 *  
	 * @param client the client to disconnect
	 */
	public void disconnect(Object client);
	
	/**
	 * Returns the possibly empty list of clients currently connected to this
	 * memory block.
	 *  
	 * @return the possibly empty list of clients currently connected to this
	 * memory block
	 */
	public Object[] getConnections();
	
	/**
	 * Dispose this memory block. Connected clients are disconnected.
	 * @throws DebugException if the memory block cannot be disposed.
	 */
	public void dispose() throws DebugException;
	
	/**
	 * Returns the origin of this memory block.
	 * 
	 * @return the origin of this memory block
	 */
	public IMemoryBlockRetrieval getMemoryBlockRetrieval();
	
	/**
	 * Returns this memory block's addressable size in number of bytes. The addressable size
	 * of memory block indicates the minimum number of bytes that can be retrieved as
	 * a single unit.
	 *  
	 * @return this memory block's addressable size
	 * @throws DebugException if the addressable size cannot be obtained.
	 */
	public int getAddressableSize() throws DebugException;
}
