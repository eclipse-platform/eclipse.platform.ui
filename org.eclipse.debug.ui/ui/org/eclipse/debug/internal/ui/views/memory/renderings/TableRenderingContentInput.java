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
package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;

import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.ui.memory.AbstractTableRendering;

/**
 * This is an internal class for storing information about the content
 * in the table viewer.
 */
public class TableRenderingContentInput {

	private AbstractTableRendering fRendering;
	private int fPreBuffer;					// number of lines before the top visible line
	private int fPostBuffer;				// number of lines after thes last visible line
	private int fDefaultBufferSize;
	private BigInteger fStartingAddress;	// start address to the buffer
	private int fNumVisibleLines;			// number of visible lines
	private boolean fUpdateDelta;			// should the content provider calculate delta info
	private BigInteger fMemoryBlockBaseAddress;		// base address of the memory block when this input is set
	
	public TableRenderingContentInput(AbstractTableRendering rendering, int preBuffer, int postBuffer, int defaultBufferSize, BigInteger startAddress, int numOfLines, boolean updateDelta)
	{
		fRendering = rendering;
		fPreBuffer = preBuffer;
		fPostBuffer = postBuffer;
		fStartingAddress = startAddress;
		fNumVisibleLines = numOfLines;
		fDefaultBufferSize = defaultBufferSize;
		fUpdateDelta = updateDelta;

		updateContentBaseAddress();
	}
	public int getNumVisibleLines() {
		return fNumVisibleLines;
	}
	public int getPostBuffer() {
		return fPostBuffer;
	}
	public int getPreBuffer() {
		return fPreBuffer;
	}
	public BigInteger getStartingAddress() {
		return fStartingAddress;
	}
	
	public IMemoryBlock getMemoryBlock()
	{
		return fRendering.getMemoryBlock();
	}
	public void setPostBuffer(int postBuffer) {
		fPostBuffer = postBuffer;
	}
	public void setPreBuffer(int preBuffer) {
		fPreBuffer = preBuffer;
	}
	public int getDefaultBufferSize() {
		return fDefaultBufferSize;
	}
	public void setDefaultBufferSize(int defaultBufferSize) {
		fDefaultBufferSize = defaultBufferSize;
	}
	public boolean isUpdateDelta() {
		return fUpdateDelta;
	}
	public void setUpdateDelta(boolean updateDelta) {
		fUpdateDelta = updateDelta;
	}
	public AbstractTableRendering getMemoryRendering()
	{
		return fRendering;
	}
	public void setStartingAddress(BigInteger address)
	{
		fStartingAddress = address;
	}
	public BigInteger getContentBaseAddress() {
		return fMemoryBlockBaseAddress;
	}
	public void updateContentBaseAddress() {
		IMemoryBlock memoryBlock = fRendering.getMemoryBlock();
		if (memoryBlock instanceof IMemoryBlockExtension)
			fMemoryBlockBaseAddress = ((IMemoryBlockExtension)memoryBlock).getBigBaseAddress();
		else
			fMemoryBlockBaseAddress = BigInteger.valueOf(memoryBlock.getStartAddress());
	}
}
