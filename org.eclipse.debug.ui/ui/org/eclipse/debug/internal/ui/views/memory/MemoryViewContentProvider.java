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

package org.eclipse.debug.internal.ui.views.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.core.memory.IExtendedMemoryBlock;
import org.eclipse.debug.internal.core.memory.IExtendedMemoryBlockRetrieval;
import org.eclipse.debug.internal.core.memory.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.TabItem;

/**
 * Content provider for MemoryViewTab
 * 
 * @since 3.0
 */
public class MemoryViewContentProvider extends BasicDebugViewContentProvider {
	
	private static final String PREFIX = "MemoryViewContentProvider."; //$NON-NLS-1$
	private static final String UNABLE_TO_RETRIEVE_CONTENT = PREFIX + "Unable_to_retrieve_content"; //$NON-NLS-1$

	private static final String DEFAULT_PADDED_STR = "--"; //$NON-NLS-1$
		
	// cached information
	protected Vector lineCache;
	
	// keeps track of all memory line ever retrieved
	// allow us to compare and compute deltas
	protected Hashtable contentCache;
	
	private BigInteger fBufferTopAddress;
	private BigInteger fBaseAddress;
	
	private IMemoryBlock fMemoryBlock;
	private TabItem fTabItem;
	private MemoryViewTab fViewTab;
	private boolean fLockRefresh = false;
	
	// implementation of MemoryByte for IMemoryBlock Support
	private class MByte extends MemoryByte
	{
		protected MByte(byte value, byte flags)
		{
			this.value = value;
			this.flags = flags;
		}
		
		protected MByte()
		{
			
		}
	}
	
	/**
	 * @param memoryBlock
	 * @param newTab
	 */
	public MemoryViewContentProvider(IMemoryBlock memoryBlock, TabItem newTab)
	{
		fMemoryBlock = memoryBlock;
		fTabItem = newTab;
		lineCache = new Vector();
		contentCache = new Hashtable();
		
		fViewTab = (MemoryViewTab)fTabItem.getData();
			
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	/**
	 * @param viewer
	 */
	public void setViewer(StructuredViewer viewer)
	{
		fViewer = viewer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {

		// fTabItem disposed by view tab
		
		DebugPlugin.getDefault().removeDebugEventListener(this);		
		
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {

		// if cache is empty, get memory
		if (lineCache.isEmpty()) { 
		
			try {
				if (fMemoryBlock instanceof IExtendedMemoryBlock)
				{
					// calculate top buffered address
					BigInteger address = ((IExtendedMemoryBlock)fMemoryBlock).getBigBaseAddress();
					
					if (address == null)
					{
						address = new BigInteger("0"); //$NON-NLS-1$
					}
					
					MemoryViewTab viewTab = (MemoryViewTab)fTabItem.getData();
					
					BigInteger bigInt = address;
					if (bigInt.compareTo(BigInteger.valueOf(32)) <= 0) {
						viewTab.TABLE_PREBUFFER = 0;
					} else {
						viewTab.TABLE_PREBUFFER = bigInt.divide(BigInteger.valueOf(32)).min(BigInteger.valueOf(viewTab.TABLE_DEFAULTBUFFER)).intValue();
					}
				
					address = bigInt.subtract(BigInteger.valueOf(fViewTab.getBytesPerLine()*viewTab.TABLE_PREBUFFER));
				
					// get stoarage to fit the memory view tab size
					getMemoryToFitTable(address, fViewTab.getNumberOfVisibleLines()+viewTab.TABLE_PREBUFFER+viewTab.TABLE_POSTBUFFER, true);
				}
				else
				{
					// get as much memory as the memory block can handle
					MemoryViewTab viewTab = (MemoryViewTab)fTabItem.getData();
					viewTab.TABLE_PREBUFFER=0;
					viewTab.TABLE_POSTBUFFER=0;
					viewTab.TABLE_DEFAULTBUFFER=0;
					
					long startAddress = fMemoryBlock.getStartAddress();					
					BigInteger address = BigInteger.valueOf(startAddress);
					
					long length = fMemoryBlock.getLength();
					long numLines = length / fViewTab.getBytesPerLine();
					
					getMemoryToFitTable(address, numLines, true);
				}
			} catch (DebugException e) {
				DebugUIPlugin.log(e.getStatus());
				((MemoryViewTab)fTabItem.getData()).displayError(e);
				return lineCache.toArray();
			}
		}
		return lineCache.toArray();
	}
	
	/**
	 * @return
	 */
	public IMemoryBlock getMemoryBlock() {
		return fMemoryBlock;
	}
	
	/**
	 * Get memory to fit table
	 * @param startingAddress
	 * @param numberOfLines
	 * @param updateDelta
	 * @throws DebugException
	 */
	public void getMemoryToFitTable(BigInteger startingAddress, long numberOfLines, boolean updateDelta) throws DebugException
	{
		boolean error = false;
		DebugException dbgEvt = null;
		
		// calculate address size
		String adjustedAddress = startingAddress.toString(16);
		
		int addressSize = getAddressSize(startingAddress);
		
		int addressLength = addressSize * IInternalDebugUIConstants.CHAR_PER_BYTE;

		// align starting address with double word boundary
		if (fMemoryBlock instanceof IExtendedMemoryBlock)
		{
			if (!adjustedAddress.endsWith("0")) //$NON-NLS-1$
			{
				adjustedAddress = adjustedAddress.substring(0, adjustedAddress.length() - 1);
				adjustedAddress += "0"; //$NON-NLS-1$
				startingAddress = new BigInteger(adjustedAddress, 16);
			}
		}

		IExtendedMemoryBlock extMemoryBlock = null;
		MemoryByte[] memoryBuffer = null;
		
		// required number of bytes
		long reqNumBytes = fViewTab.getBytesPerLine() * numberOfLines;
		String paddedString = DEFAULT_PADDED_STR;
		
		try
		{
			if (fMemoryBlock instanceof IExtendedMemoryBlock)
			{
				// get memory from memory block
				extMemoryBlock = (IExtendedMemoryBlock) fMemoryBlock;
				
				memoryBuffer =	extMemoryBlock.getBytesFromAddress(startingAddress,	reqNumBytes);
				
				if(memoryBuffer == null)
				{
					DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString(UNABLE_TO_RETRIEVE_CONTENT), null));
					throw e;
				}
				
				// get padded string
				IMemoryBlockRetrieval retrieval = extMemoryBlock.getMemoryBlockRetrieval();
				if (retrieval != null && retrieval instanceof IExtendedMemoryBlockRetrieval)
				{
					paddedString = ((IExtendedMemoryBlockRetrieval)retrieval).getPaddedString();
					
					if (paddedString == null)
					{
						paddedString = DEFAULT_PADDED_STR;
					}
				}
			}
			else
			{
				// get memory from memory block
				byte[] memory = fMemoryBlock.getBytes();
				
				if (memory == null)
				{
					DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString(UNABLE_TO_RETRIEVE_CONTENT), null));	
					throw e;					
				}
				
				// create memory byte for IMemoryBlock
				memoryBuffer = new MemoryByte[memory.length];
				for (int i=0; i<memory.length; i++)
				{
					MByte tmp = new MByte();
					tmp.value = memory[i];
					tmp.flags |= MemoryByte.VALID;
					memoryBuffer[i] = tmp;
				}
				
				paddedString = DEFAULT_PADDED_STR;
			}
		}
		catch (DebugException e)
		{
			memoryBuffer = makeDummyContent(numberOfLines);
			
			// finish creating the content provider before throwing an event
			error = true; 
			dbgEvt = e;
		}
		
		// if debug adapter did not return enough memory, create dummy memory
		if (memoryBuffer.length < reqNumBytes)
		{
			ArrayList newBuffer = new ArrayList();
			
			for (int i=0; i<memoryBuffer.length; i++)
			{
				newBuffer.add(memoryBuffer[i]);
			}
			
			for (int i=memoryBuffer.length; i<reqNumBytes; i++)
			{
				byte value = 0;
				byte flags = 0;
				flags |= MemoryByte.READONLY;
				newBuffer.add(new MByte(value, flags));
			}
			
		}
		
		// clear line cacheit'
		if (!lineCache.isEmpty())
		{
			lineCache.clear();
		}
		String address = startingAddress.toString(16);
		// save address of the top of buffer
		fBufferTopAddress = startingAddress;
		if (fMemoryBlock instanceof IExtendedMemoryBlock)
			fBaseAddress = ((IExtendedMemoryBlock) fMemoryBlock).getBigBaseAddress();
		else
			fBaseAddress = BigInteger.valueOf(fMemoryBlock.getStartAddress());
		
		if (fBaseAddress == null)
			fBaseAddress = new BigInteger("0"); //$NON-NLS-1$
		
			
		// update tab name in case base address has changed
		fViewTab.setTabName(fMemoryBlock, true);
		
		boolean manageDelta = true;
		
		// If change information is not managed by the memory block
		// The view tab will manage it and calculate delta information
		// for its content cache.
		if (fMemoryBlock instanceof IExtendedMemoryBlock)
		{
			manageDelta = !((IExtendedMemoryBlock)fMemoryBlock).isMemoryChangesManaged();
		}
			
		// put memory information into MemoryViewLine
		for (int i = 0; i < numberOfLines; i++)
		{ //chop the raw memory up 
			String tmpAddress = address.toUpperCase();
			if (tmpAddress.length() < addressLength)
			{
				for (int j = 0; tmpAddress.length() < addressLength; j++)
				{
					tmpAddress = "0" + tmpAddress; //$NON-NLS-1$
				}
			}
			MemoryByte[] memory = new MemoryByte[fViewTab.getBytesPerLine()];
			boolean isMonitored = true;
			
			// counter for memory, starts from 0 to number of bytes per line
			int k = 0;
			// j is the counter for memArray, memory returned by debug adapter
			for (int j = i * fViewTab.getBytesPerLine();
				j < i * fViewTab.getBytesPerLine() + fViewTab.getBytesPerLine();
				j++)
			{
				
				byte changeFlag = memoryBuffer[j].flags;
				if (manageDelta)
				{
					// turn off both change and unchanged bits to make sure that
					// the change bits returned by debug adapters do not take
					// any effect
					
					changeFlag |= MemoryByte.CHANGED;
					changeFlag ^= MemoryByte.CHANGED;
					
					changeFlag |= MemoryByte.UNCHANGED;
					changeFlag ^= MemoryByte.UNCHANGED;
				}
				
				MByte newByteObj = new MByte(memoryBuffer[j].value, changeFlag);
				memory[k] =  newByteObj;
				k++;
				
				
				if (!manageDelta)
				{
					// if byte is not changed nor unchanged, we do not currently have 
					// history for it.
					if ((memoryBuffer[j].flags & MemoryByte.CHANGED) == 0 &&
						(memoryBuffer[j].flags & MemoryByte.UNCHANGED) == 0)
					{
						isMonitored = false;
					}
				}
			}
			
			MemoryViewLine newLine = new MemoryViewLine(tmpAddress, memory, lineCache.size(), paddedString);
			
			MemoryViewLine oldLine = (MemoryViewLine)contentCache.get(newLine.getAddress());
			
			if (manageDelta)
			{
				if (oldLine != null)
					newLine.isMonitored = true;
			}
			else
			{
				// check the byte for information
				newLine.isMonitored = isMonitored;
			}
			
			// calculate delta info for the memory view line
			if (manageDelta)
			{
				if (updateDelta)
				{
					if (oldLine != null)
					{
						newLine.markDeltas(oldLine);
					}
				}
				else
				{
					if (oldLine != null)
					{
						// deltas can only be reused if the line has not been changed
						// otherwise, force a refresh
						if (newLine.isLineChanged(oldLine))
						{
							newLine.markDeltas(oldLine);
						}
						else
						{
							newLine.copyDeltas(oldLine);
						}
					}
				}
			}
			lineCache.add(newLine);
			
//			// add to contentCache
//			contentCache.put(newLine.getAddress(), newLine);
			
			// increment row address
			BigInteger bigInt = new BigInteger(address, 16);
			address = bigInt.add(BigInteger.valueOf(fViewTab.getBytesPerLine())).toString(16);
		}
		
		if (error){
			throw dbgEvt;
		}
	}
	
	/**
	 * @param numberOfLines
	 * @return
	 */
	private MemoryByte[] makeDummyContent(long numberOfLines) {
		MemoryByte[] memoryBuffer;
		// make up dummy memory, needed for recovery in case the debug adapter
		// is capable of retrieving memory again

		int numBytes = (int)(fViewTab.getBytesPerLine() * numberOfLines);
		memoryBuffer = new MemoryByte[numBytes];
		
		for (int i=0; i<memoryBuffer.length; i++){
			memoryBuffer[i] = new MByte();
			memoryBuffer[i].value = 0;
			memoryBuffer[i].flags |= MemoryByte.READONLY;
		}
		return memoryBuffer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.internal.views.BasicDebugViewContentProvider#doHandleDebugEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected void doHandleDebugEvent(DebugEvent event) {
		
		// do nothing if the debug event did not come from a debug element comes from non-debug element
		if (!(event.getSource() instanceof IDebugElement))
			return;
		
		IDebugElement src = (IDebugElement)event.getSource();
		
		// if a debug event happens from the memory block
		// invoke contentChanged to get content of the memory block updated
		if (event.getKind() == DebugEvent.CHANGE && event.getSource() == fMemoryBlock)
		{
			if (event.getDetail() == DebugEvent.STATE){
				fViewTab.updateLabels();
			}
			else
			{	
				updateContent();
			}
		}
		
		// if the suspend evnet happens from the debug target that the blocked
		// memory block belongs to
		if (event.getKind() == DebugEvent.SUSPEND && src.getDebugTarget() == fMemoryBlock.getDebugTarget())
		{	
			updateContent();
		}

	}
	
	/**
	 * Checks to see if the content needs to be refreshed
	 * TODO: this methd is never called
	 * @return
	 */
	protected boolean isRefreshNeeded()
	{	
		boolean refreshNeeded = false;
		try
		{	
			if (isBaseAddressChanged())
				return true;

			MemoryViewLine[] cached = (MemoryViewLine[])lineCache.toArray(new MemoryViewLine[lineCache.size()]);
							
			// convert IMemory to a flat array of MemoryBlockByte
			ArrayList newMemory = new ArrayList();
				
			if (!(fMemoryBlock instanceof IExtendedMemoryBlock))
			{
				byte[] memory = fMemoryBlock.getBytes();
				
				if (memory == null)
				{
					DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString(UNABLE_TO_RETRIEVE_CONTENT), null));
					throw e;					
				}
				
				// create memory byte for IMemoryBlock
				for (int i=0; i<memory.length; i++)
				{
					MByte tmp = new MByte();
					tmp.value = memory[i];
					tmp.flags |= MemoryByte.VALID;
					newMemory.add(tmp);
				}
			}
			else
			{		
			
				IExtendedMemoryBlock extMB = (IExtendedMemoryBlock)fMemoryBlock;
				
				MemoryByte[] memory = extMB.getBytesFromAddress(fBufferTopAddress, lineCache.size()*fViewTab.getBytesPerLine());
				
				if (memory == null)
				{
					DebugException e = new DebugException(DebugUIPlugin.newErrorStatus(DebugUIMessages.getString(UNABLE_TO_RETRIEVE_CONTENT), null));
					throw e;
				}
				
				for (int i=0; i<memory.length; i++)
				{
					newMemory.add(memory[i]);
				}				
			}		
							
			// compare each byte, if one of the bytes is not the same, refresh view tab
			for (int i=0; i<newMemory.size(); i++)
			{
				MemoryByte newByte = (MemoryByte)newMemory.get(i);

				if (i/fViewTab.getBytesPerLine() >= cached.length)
				{
					// if cache cannot be located, need refresh
					refreshNeeded = true;
					break;
				}
				
				MemoryViewLine lineToCheck = cached[i/fViewTab.getBytesPerLine()];
				MemoryByte oldByte = lineToCheck.getByte(i%fViewTab.getBytesPerLine());
				
				// if a byte becomes available or unavailable
				if ((newByte.flags & MemoryByte.VALID)!= (oldByte.flags & MemoryByte.VALID))
				{
					refreshNeeded = true;
					break;
				}
				if (((newByte.flags & MemoryByte.VALID) == MemoryByte.VALID) && 
						((oldByte.flags & MemoryByte.VALID)==MemoryByte.VALID))
				{
					// compare value if both bytes are available
					if (newByte.value != oldByte.value)
					{
						refreshNeeded = true;
						break;
					}
				}
			}
			
		}
		catch (DebugException e)
		{
			fViewTab.displayError(e);
			return false;
		}
		return refreshNeeded;
	}
	
	/**
	 * @return if the base address of the memory block has changed
	 */
	private boolean isBaseAddressChanged()
	{
		if (!(fMemoryBlock instanceof IExtendedMemoryBlock))
			return false;
		
		IExtendedMemoryBlock extMB = (IExtendedMemoryBlock)fMemoryBlock;
			
		// if base address has changed, refresh is needed
		BigInteger newBaseAddress = extMB.getBigBaseAddress();
		
		if (newBaseAddress == null)
		{	
			newBaseAddress = new BigInteger("0"); //$NON-NLS-1$
		}
		
		if (newBaseAddress.compareTo(fBaseAddress) != 0) {
			return true;
		}
		return false;			
	}
	
	public void forceRefresh()
	{
		if (!fLockRefresh) {
			fLockRefresh = true;
			refresh();
			fLockRefresh = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.internal.views.BasicDebugViewContentProvider#refresh()
	 */
	protected void refresh() {
		super.refresh();
	}

	
	/**
	 * Update content of the view tab if the content of the memory block has changed
	 * or if its base address has changed
	 * Update will not be performed if the memory block has not been changed or
	 * if the view tab is disabled.
	 */
	public void updateContent()
	{
		IDebugTarget dt = fMemoryBlock.getDebugTarget();
		
		// no need to update if debug target is disconnected or terminated
		if (dt.isDisconnected() || dt.isTerminated())
		{
			return;
		}
		
		// cache content before getting new ones
		MemoryViewLine[] lines =(MemoryViewLine[]) lineCache.toArray(new MemoryViewLine[lineCache.size()]);
		if (contentCache != null)
		{
			contentCache.clear();
		}
		
		//do not handle event if the tab has been disabled
		if (!fViewTab.isEnabled())
			 return;
		
		for (int i=0; i<lines.length; i++)
		{
			contentCache.put(lines[i].getAddress(), lines[i]);
			lines[i].isMonitored = true;
		}
		
		// if view tab is enabled but block is disabled, enable the block
		if (fViewTab.getMemoryBlock() instanceof IExtendedMemoryBlock)
		{
			if (!((IExtendedMemoryBlock)fViewTab.getMemoryBlock()).isEnabled())
			{
				((IExtendedMemoryBlock)fViewTab.getMemoryBlock()).enable();
			}
		}
		boolean updateTopAddress = false;
		// if base address has changed, set cursor back to the base address
		if (isBaseAddressChanged())
		{
			updateTopAddress = true;
			if (fMemoryBlock instanceof IExtendedMemoryBlock)
			{
				BigInteger address = ((IExtendedMemoryBlock)fMemoryBlock).getBigBaseAddress();
				
				if (address == null)
					address = new BigInteger("0"); //$NON-NLS-1$
				
				fViewTab.setSelectedAddress(address, true);
			}
			else
			{
				BigInteger address = BigInteger.valueOf(fMemoryBlock.getStartAddress());
				fViewTab.setSelectedAddress(address, true);
			}	
		}			
		// reset all the deltas currently stored in contentCache
		// This will ensure that changes will be recomputed when user scrolls
		// up or down the memory view.		
		resetDeltas();
		fViewTab.refresh();
		
		if (updateTopAddress)
		{
			// top visible address may have been changed if base address has changed
			fViewTab.updateSyncTopAddress(true);
		}
	}

	/**
	 * @return
	 */
	public BigInteger getBufferTopAddress()
	{
		return fBufferTopAddress;
	}
	
	/**
	 * Calculate address size of the given address
	 * @param address
	 * @return
	 */
	public int getAddressSize(BigInteger address)
	{
		// calculate address size
		 String adjustedAddress = address.toString(16);
		
		 int addressSize = 0;
		 if (fMemoryBlock instanceof IExtendedMemoryBlock)
		 {
			 addressSize = ((IExtendedMemoryBlock)fMemoryBlock).getAddressSize();
		 }
		
		 // handle IMemoryBlock and invalid address size returned by IExtendedMemoryBlock
		 if (addressSize <= 0)
		 {
			 if (adjustedAddress.length() > 8)
			 {
				 addressSize = 8;
			 }
			 else
			 {
				 addressSize = 4;
			 }			
		 }		
		 
		 return addressSize;
	}
	
	/**
	 * @return base address of memory block
	 */
	public BigInteger getContentBaseAddress()
	{
		return fBaseAddress; 
	}
	
	/**
	 * Clear all delta information in the lines
	 */
	public void resetDeltas()
	{
		Enumeration enum = contentCache.elements();
		
		while (enum.hasMoreElements())
		{
			MemoryViewLine line = (MemoryViewLine)enum.nextElement();
			line.unmarkDeltas();
		}
	}
	
	/**
	 * Check if address is out of buffered range
	 * @param address
	 * @return
	 */
	protected boolean isAddressOutOfRange(BigInteger address)
	{
		if (lineCache != null)
		{
			MemoryViewLine first = (MemoryViewLine)lineCache.firstElement();
			MemoryViewLine last = (MemoryViewLine) lineCache.lastElement();
			
			if (first == null ||last == null)
				return true;
			
			BigInteger startAddress = new BigInteger(first.getAddress(), 16);
			BigInteger lastAddress = new BigInteger(last.getAddress(), 16);
			lastAddress = lastAddress.add(BigInteger.valueOf(fViewTab.getBytesPerLine()));
			
			if (startAddress.compareTo(address) <= 0 &&
				lastAddress.compareTo(address) >= 0)
			{
				return false;
			}
			return true;
		}
		return true;
	}
}
