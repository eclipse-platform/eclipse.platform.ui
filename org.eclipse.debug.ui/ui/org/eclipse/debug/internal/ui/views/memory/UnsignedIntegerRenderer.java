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

import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.core.memory.IMemoryRendering;
import org.eclipse.debug.internal.core.memory.MemoryBlockManager;

/**
 * Converts bytes to unsigned integer and vice versa
 */
public class UnsignedIntegerRenderer extends AbstractMemoryRenderer {
	
	ITableMemoryViewTab fTableViewTab;

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.AbstractMemoryRenderer#setViewTab(com.ibm.debug.extended.ui.IMemoryViewTab)
	 */
	public void setViewTab(IMemoryViewTab viewTab) {
		super.setViewTab(viewTab);
		
		if (viewTab instanceof ITableMemoryViewTab){
			fTableViewTab = (ITableMemoryViewTab)viewTab;
		}
	}

	private String convertToString(byte[] byteArray, int columnSize, int endianess)
	{
		String ret;
		long result = 0;
		
		if (columnSize == 1)
		{
			result = byteArray[0];
			result &= 0xff;
		}
		else if (columnSize == 2)
		{	
			result = RendererUtil.convertByteArrayToInt(byteArray, endianess);
		}
		else if (columnSize == 4)
		{
			result = RendererUtil.convertByteArrayToLong(byteArray, endianess);
		}
		else if (columnSize == 8)
		{
			BigInteger value = RendererUtil.convertByteArrayToUnsignedLong(byteArray, endianess);
			return value.toString();				
		}
		else
		{
			BigInteger bigRet = RendererUtil.convertByteArrayToUnsignedBigInteger(byteArray, endianess);
			return bigRet.toString();
		}

		ret = new Long(result).toString();
		
		return ret;
	}	
	
	private byte[] convertToBytes(int colSize, String newValue, int endianess)
	{
		try {
			byte[] bytes;
			if (colSize == 1)
			{
				short i = Short.parseShort(newValue);
				bytes = RendererUtil.convertShortToByteArray(i, endianess);
				bytes = extractBytes(bytes, endianess, colSize);
			}
			// unsigned integer
			else if (colSize == 2)
			{	
				int i = Integer.parseInt(newValue);
				bytes = RendererUtil.convertIntToByteArray(i, endianess);
				bytes = extractBytes(bytes, endianess, colSize);
			}
			else if (colSize == 4)
			{	
				long i = Long.parseLong(newValue);
				bytes = RendererUtil.convertLongToByteArray(i, endianess);
				bytes = extractBytes(bytes, endianess, colSize);
			}
			else if (colSize == 8)
			{	
				BigInteger i = new BigInteger(newValue);
				bytes = RendererUtil.convertBigIntegerToByteArray(i, endianess);
				bytes = extractBytes(bytes, endianess, colSize);
			}
			else
			{	
				BigInteger i = new BigInteger(newValue);
				bytes = RendererUtil.convertUnsignedBigIntegerToByteArray(i, endianess);
				bytes = extractBytes(bytes, endianess, colSize);

				return bytes;
			}
			
			return bytes;
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	
	/**
	 * @return current endianess
	 */
	private int getEndianess() {
		// default to Big Endian in case the endianess cannot be determined
		int endianess = RendererUtil.BIG_ENDIAN;
		
		// if it's IMemoryBlock
		// Check current state of the rendering
		IMemoryRendering[] renderings = MemoryBlockManager.getMemoryRenderingManager().getRenderings(fViewTab.getMemoryBlock(), fViewTab.getRenderingId());
		
		if (renderings.length > 0){
			if (renderings[0] instanceof IntegerRendering)
			{
				endianess = ((IntegerRendering)renderings[0]).getCurrentEndianess();
			}
		}
		return endianess;
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.AbstractMemoryRenderer#getString(java.lang.String, java.math.BigInteger, byte[])
	 */
	public String getString(String dataType, BigInteger address, MemoryByte[] data, String paddedStr) {
		
		boolean invalid = false;
		for (int i=0; i<data.length; i++)
		{
			if (!data[i].isValid())
			{
				invalid = true;
				break;
			}
		}
		
		if (invalid)
		{
			StringBuffer strBuf = new StringBuffer();
			for (int i=0; i<data.length; i++)
			{
				strBuf.append(paddedStr);
			}
			return strBuf.toString();
		}
		
		if (fTableViewTab != null){
			int columnSize = fTableViewTab.getColumnSize();
			int endianess = getEndianess();
			
			byte[] byteArray = new byte[data.length];
			for (int i=0; i<byteArray.length;i ++)
			{
				byteArray[i] = data[i].getValue();
			}
			
			return convertToString(byteArray, columnSize, endianess);
		}
        return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.AbstractMemoryRenderer#getBytes(java.lang.String, java.math.BigInteger, java.lang.String)
	 */
	public byte[] getBytes(String dataType, BigInteger address, MemoryByte[] currentValues, String data) {
		
		if (fTableViewTab != null){
			int columnSize = fTableViewTab.getColumnSize();
			int endianess = getEndianess();
			
			return convertToBytes(columnSize, data, endianess);
		}
        return new byte[0];
	}
	
	private byte[] extractBytes(byte[] bytes, int endianess, int colSize) {
		
		if (colSize > bytes.length)
			throw new NumberFormatException();
		
		// take the least significant 'colSize' bytes out of the bytes array
		// if it's big endian, it's the last 'colSize' bytes
		if (endianess == RendererUtil.BIG_ENDIAN)
		{	
			// check most significan bytes... if data has to be represented
			// using more than 'colSize' number of bytes, this
			// number is invalid, throw number format exception
			for (int i=0; i<colSize; i++)
			{
				if (bytes[i] != 0)
					throw new NumberFormatException();
			}
			
			byte[] copy = new byte[colSize];
			for (int j=0, k=bytes.length-colSize; j<copy.length && k<bytes.length; j++, k++)
			{	
				copy[j] = bytes[k]; 
			}
			bytes = copy;
		}
		// if it's little endian, it's the first 'colSize' bytes
		else
		{
			// check most significan bytes... if data has to be represented
			// using more than 'colSize' number of bytes, this
			// number is invalid, throw number format exception
			for (int i=colSize; i<bytes.length; i++)
			{
				if (bytes[i] != 0)
					throw new NumberFormatException();
			}
			
			byte[] copy = new byte[colSize];
			for (int j=0; j<copy.length; j++)
			{	
				copy[j] = bytes[j]; 
			}
			bytes = copy;							
		}
		return bytes;
	}	
}
