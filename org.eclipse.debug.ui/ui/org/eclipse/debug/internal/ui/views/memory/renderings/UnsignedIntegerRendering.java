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
package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;

import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Represent unsigned integer rendering
 */
public class UnsignedIntegerRendering extends AbstractIntegerRendering {

	/**
	 * @param memBlock
	 * @param renderingId
	 */
	public UnsignedIntegerRendering(String renderingId) {
		super(renderingId);
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
			result = RenderingsUtil.convertByteArrayToInt(byteArray, endianess);
		}
		else if (columnSize == 4)
		{
			result = RenderingsUtil.convertByteArrayToLong(byteArray, endianess);
		}
		else if (columnSize == 8)
		{
			BigInteger value = RenderingsUtil.convertByteArrayToUnsignedLong(byteArray, endianess);
			return value.toString();				
		}
		else if (columnSize == 16)
		{
			BigInteger bigRet = RenderingsUtil.convertByteArrayToUnsignedBigInt(byteArray, endianess);
			return bigRet.toString();
		}
		else
		{
			BigInteger bigRet = RenderingsUtil.convertByteArrayToUnsignedBigInt(byteArray, endianess, columnSize);
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
				bytes = RenderingsUtil.convertShortToByteArray(i, endianess);
				bytes = extractBytes(bytes, endianess, colSize);
			}
			// unsigned integer
			else if (colSize == 2)
			{	
				int i = Integer.parseInt(newValue);
				bytes = RenderingsUtil.convertIntToByteArray(i, endianess);
				bytes = extractBytes(bytes, endianess, colSize);
			}
			else if (colSize == 4)
			{	
				long i = Long.parseLong(newValue);
				bytes = RenderingsUtil.convertLongToByteArray(i, endianess);
				bytes = extractBytes(bytes, endianess, colSize);
			}
			else if (colSize == 8)
			{	
				BigInteger i = new BigInteger(newValue);
				bytes = RenderingsUtil.convertBigIntegerToByteArray(i, endianess);
				bytes = extractBytes(bytes, endianess, colSize);
			}
			else if (colSize == 16)
			{	
				BigInteger i = new BigInteger(newValue);
				bytes = RenderingsUtil.convertUnsignedBigIntegerToByteArray(i, endianess);
				bytes = extractBytes(bytes, endianess, colSize);

				return bytes;
			}
			else
			{
				BigInteger i = new BigInteger(newValue);
				bytes = RenderingsUtil.convertUnsignedBigIntToByteArray(i, endianess, colSize);
				bytes = extractBytes(bytes, endianess, colSize);
				return bytes;				
			}
			
			return bytes;
		} catch (NumberFormatException e) {
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.AbstractMemoryRenderer#getString(java.lang.String, java.math.BigInteger, byte[])
	 */
	public String getString(String dataType, BigInteger address, MemoryByte[] data) {
		
		String paddedStr = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugUIConstants.PREF_PADDED_STR);
		boolean invalid = false;
		for (int i=0; i<data.length; i++)
		{
			if (!data[i].isReadable())
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
		
		int columnSize = getBytesPerColumn();
		int endianess = getDisplayEndianess();
		if (endianess == RenderingsUtil.ENDIANESS_UNKNOWN)
			endianess = getBytesEndianess(data);
		
		byte[] byteArray = new byte[data.length];
		for (int i=0; i<byteArray.length;i ++)
		{
			byteArray[i] = data[i].getValue();
		}
		
		// if endianess is unknown, do not render, just return padded string		
		if (RenderingsUtil.ENDIANESS_UNKNOWN == endianess)
		{
			StringBuffer strBuf = new StringBuffer();
			for (int i=0; i<byteArray.length; i++)
			{
				strBuf.append(paddedStr);
			}
			return strBuf.toString();
		}
		
		return convertToString(byteArray, columnSize, endianess);
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.AbstractMemoryRenderer#getBytes(java.lang.String, java.math.BigInteger, java.lang.String)
	 */
	public byte[] getBytes(String dataType, BigInteger address, MemoryByte[] currentValues, String data) {
		
		int columnSize = getBytesPerColumn();
		int endianess = getDisplayEndianess();
		if (endianess == RenderingsUtil.ENDIANESS_UNKNOWN)
			endianess = getBytesEndianess(currentValues);
		
		// if endianess is unknown, do not try to render new data to bytes
		if (endianess == RenderingsUtil.ENDIANESS_UNKNOWN)
		{
			byte[] retBytes = new byte[currentValues.length];
			for (int i=0 ;i<currentValues.length; i++)
				retBytes[i] = currentValues[i].getValue();
			return retBytes;
		}
		
		return convertToBytes(columnSize, data, endianess);
	}
	
	private byte[] extractBytes(byte[] bytes, int endianess, int colSize) {
		
		if (colSize > bytes.length)
			throw new NumberFormatException();
		
		// take the least significant 'colSize' bytes out of the bytes array
		// if it's big endian, it's the last 'colSize' bytes
		if (endianess == RenderingsUtil.BIG_ENDIAN)
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
