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

import java.util.ArrayList;

import org.eclipse.debug.core.model.MemoryByte;

/**
 * @since 3.0
 */

public class MemoryViewLine extends Object {
	private String fAddress;
	private String fStrRep;
	private MemoryByte[] fBytes;
	private byte[] fByteArray; 
	private int fTableIndex = -1;
	private String fPaddedString;
	public boolean isMonitored;

	public static final String P_ADDRESS = "address"; //$NON-NLS-1$

	// for raw hex data, it's 2 characters per byte
	private static final int numCharPerByteForHex = 2;

	public MemoryViewLine(String address, MemoryByte[] bytes, int tableIndex, String paddedString) {
		fAddress = address;
		fBytes = bytes;
		fTableIndex = tableIndex;
		fPaddedString = paddedString;
	}

	public String getAddress() {
		return fAddress;
	}

	public void setAddress(String address) {
		fAddress = address;
	}
	
	public MemoryByte[] getBytes()
	{
		return fBytes;
	}
	
	public MemoryByte getByte(int offset)
	{
		if (fBytes == null)
			return null;
		
		if (offset < fBytes.length) {
			return fBytes[offset];
		}
		
		return null;		
	}
	
	public MemoryByte[] getBytes(int start, int end)
	{
		ArrayList ret = new ArrayList();
		
		for (int i=start; i<end; i++)
		{
			ret.add(fBytes[i]);
		}
		return (MemoryByte[]) ret.toArray(new MemoryByte[ret.size()]);
	}
	
	public String getRawMemoryString()
	{
		if (fStrRep == null)
		{
			StringBuffer buffer = new StringBuffer();
			fStrRep = HexRenderer.convertByteArrayToHexString(getByteArray());
			fStrRep = fStrRep.toUpperCase();
			
			buffer = buffer.append(fStrRep);
			
			// pad unavailable bytes with padded string from memory block
			String paddedString = null;
			int bufferCounter = 0;
			for (int i=0; i<fBytes.length; i++)
			{ 
				// if byte is invalid
				if (!fBytes[i].isValid())
				{
					if (paddedString == null)
					{
						paddedString = fPaddedString;
						
						if (paddedString.length() > MemoryViewLine.numCharPerByteForHex)
							paddedString = paddedString.substring(0, MemoryViewLine.numCharPerByteForHex);
					}
					buffer.replace(bufferCounter, bufferCounter+MemoryViewLine.numCharPerByteForHex, paddedString);		
					bufferCounter += MemoryViewLine.numCharPerByteForHex;
				}
			}
			
			fStrRep = buffer.toString();
		}
		
		return fStrRep;
	}
	
	/**
	 * @param start
	 * @param end
	 * @return padded string
	 */
	public String getPaddedString(int start, int end) {
		StringBuffer buf = new StringBuffer();
		
		for (int i=start; i<end; i++)
		{	
			buf.append(fPaddedString);
		}
		return buf.toString();
	}
	
	public String getPaddedString()
	{
		return fPaddedString;
	}

	/**
	 * @param start
	 * @param end
	 * @return is the bytes between start and end are all valid
	 */
	public boolean isAvailable(int start, int end) {
		boolean available = true;
		for (int i=start; i<end; i++)
		{	
			if (!fBytes[i].isValid())
			{	
				available = false;
				break;
			}
		}
		return available;
	}


	public byte[] getByteArray()
	{
		if (fByteArray == null)
		{
			fByteArray = new byte[fBytes.length];
			for (int i=0; i<fBytes.length; i++)
			{
				fByteArray[i] = fBytes[i].getValue();
			}			
		}
		
		return fByteArray;
	}
	
	public byte[] getByteArray(int start, int end)
	{
		byte[] ret = new byte[end-start];
		int j=0;
		
		for (int i=start; i<end; i++)
		{
			ret[j] = fBytes[i].getValue();
			j++;
		}
		return ret;
	}
	
	public void markDeltas(MemoryViewLine oldData)
	{
		if (oldData == null)
			return;
		
		// if address is not the same, no need to compare
		if (!oldData.getAddress().equals(this.getAddress()))
			return;
		
		// if the string representation is the same, no need to compare
		if (oldData.getRawMemoryString().equals(getRawMemoryString()))
			return;
		
		MemoryByte[] oldMemory = oldData.getBytes();
		
		if (oldMemory.length != fBytes.length)
			return;
			
		for (int i=0; i<fBytes.length; i++)
		{
			// turn off unknown bit
			fBytes[i].setUnknown(false);
			
			if ((fBytes[i].getFlags() & MemoryByte.VALID) != (oldMemory[i].getFlags() & MemoryByte.VALID))
			{
				fBytes[i].setChanged(true);
				continue;
			}
				
			if (fBytes[i].isValid() && oldMemory[i].isValid())
			{
				if (fBytes[i].getValue() != oldMemory[i].getValue())
				{
					fBytes[i].setChanged(true);
				}
			}
		}
	}
	
	public void copyDeltas(MemoryViewLine oldData)
	{
		if (oldData == null)
			return;
		
		// if address is not the same, do not copy
		if (!oldData.getAddress().equals(this.getAddress()))
			return;
		
		// reuse delta information from old data
		MemoryByte[] oldMemory = oldData.getBytes();
		
		if (oldMemory.length != fBytes.length)
			return;
			
		for (int i=0; i<fBytes.length; i++)
		{
			fBytes[i].setFlags(oldMemory[i].getFlags());
		}		
	}
	
	public boolean isLineChanged(MemoryViewLine oldData)
	{
		if (oldData == null)
			return false;
		
		// if address is not the same, no need to compare
		if (!oldData.getAddress().equals(this.getAddress()))
			return false;
		
		// if the string representation is not the same, this line has changed
		if (oldData.getRawMemoryString().equals(getRawMemoryString())) {
			return false;
		}
		return true;
	}
	
	/**
	 * @param offset
	 * @param endOffset
	 * @return true if the specified range of memory has changed, false otherwise
	 * */
	
	public boolean isRangeChange(int offset, int endOffset)
	{	
		byte ret = fBytes[offset].getFlags();
		for (int i=offset; i<=endOffset; i++)
		{
			ret |= fBytes[i].getFlags();
		}
		
		if ((ret&MemoryByte.CHANGED) == MemoryByte.CHANGED &&
			 (ret&MemoryByte.UNKNOWN) != MemoryByte.UNKNOWN) {
			return true;
		}
		return false;
	}
	
	public void unmarkDeltas()
	{
		for (int i=0; i<fBytes.length; i++)
		{
			// unset the change bit
			if (fBytes[i].isChanged())
				fBytes[i].setChanged(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(getAddress());
		
		buf.append(": "); //$NON-NLS-1$
		
		buf.append(getRawMemoryString());
		
		return buf.toString();
	}
	
	public int getTableIndex()
	{
		return fTableIndex;
	}

}

