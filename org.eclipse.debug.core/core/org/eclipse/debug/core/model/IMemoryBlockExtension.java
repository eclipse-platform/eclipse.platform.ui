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
 * TODO: notes about the memory view should be moved elsewhere, as this is specific
 * to the memory view, not memory blocks.
 *  - connects when a block is visibile
 *  - disconnects when a block is hidden
 *  - expression used to construct the tab label of the memory view
 *  - manages change state for memory blocks that do not manage their own state
 *  When firing change event, be aware of the following:
 *  - whenever a change event is fired, the content provider for Memory View
 *    view checks to see if memory has actually changed.  
 *  - If memory has actually changed, a refresh will commence.  Changes to the memory block
 *    will be computed and will be shown with the delta icons.
 *  - If memory has not changed, content will not be refreshed.  However, previous delta information 
 * 	  will be erased.  The screen will be refreshed to show that no memory has been changed.  (All
 *    delta icons will be removed.)
 * Please note that these APIs will be called multiple times by the Memory View.
 * To improve performance, debug adapters need to cache the content of its memory block and only
 * retrieve updated data when necessary.
 * </p>
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
	 * @exception DebugException if unable to retrieve this memory block's expression
	 * 
	 * TODO: why should this fail? shouldn't the expression just be a property of the
	 *  memory block?
	 */
	public String getExpression() throws DebugException;	
	
	/**
	 * Returns the base address of this memory block as a big integer.
	 * 
	 * @return the base address of this memory block
	 */
	public BigInteger getBigBaseAddress();
	
	/**
	 * Returns the hard start address of this memory block as a big integer, or 
	 * <code>null</code> if none. A <code>null</code> start address indicates that
	 * memory may be retrieved outside the bounds of this memory block's base
	 * address and length.
	 * 
	 * @return the hard start address of this memory block or <code>null</code>
	 * 
	 * TODO: is this the same as the base address? if so we could delete the method
	 * and only provide a hard end address?
	 */
	public BigInteger getMemoryBlockStartAddress();
	
	/**
	 * Returns the hard end address of this memory block as a big integer, or
	 * <code>null</code> if none. A <code>null</code> end address indicates that
	 * memory may be retrieved outside the bounds of this memory block's base
	 * address and length. 
	 * 
	 * @return the hard end address of this memory block or <code>null</code>
	 */
	public BigInteger getMemoryBlockEndAddress();
	
	/**
	 * Returns the address size of this memory block in number of bytes. The address
	 * size indicates the number of bytes used to construct an address.
	 *  
	 * @return address size in number of bytes
	 */
	public int getAddressSize();
	
	/**
	 * Returns whether the base address of this memory block can be modified.
	 * 
	 * @return whether the base address of this memory block can be modified
	 */
	public boolean supportBaseAddressModification();
	
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
	 * @param offset zero based offset into this memory block at which to start
	 *  retrieving bytes
	 *  TODO: can this offset cross an addressable boundary? os is it in "units" (number
	 *   of addressible units)
	 * @param units the number of addressible units to retrieve
	 * @return an array of bytes from this memory block based on the given offset
	 *  and number of units. The size of the array returned must to be equal to 
	 *  <code>units</code> * <code>getAddressableSize()</code>.
	 * @throws DebugException if unable to retrieve the specified bytes
	 * @see MemoryByte
	 */
	public MemoryByte[] getBytesFromOffset(long offset, long units) throws DebugException;
	
	/**
	 * Returns bytes from this memory block based on the given address and the
	 * addressable size of this memory block.
	 *   
	 * @param address address at which to begin retrieving bytes
	 *  TODO: can this address cross an addressible boundary?
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
	 * @param offset the zero based offset at which to set the new values
	 *  TODO: is this an absolute offset, or a number of addressible units? can it cross
	 *   a boundary?
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
	 * Returns whether this memory block is in big endian format.
	 * 
	 * @return whether this memory block is in big endian format
	 */
	public boolean isBigEndian();

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
	 */
	public void dispose();
	
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
	 */
	public int getAddressableSize();
}
