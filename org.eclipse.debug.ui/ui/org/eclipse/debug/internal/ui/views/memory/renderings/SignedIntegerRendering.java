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
 * 
 * Represent a signed integer rendering.
 */
public class SignedIntegerRendering extends AbstractIntegerRendering {

	private int fColSize;
	private BigInteger fMax;
	private BigInteger fMin;
	
	/**
	 * @param memBlock
	 * @param renderingId
	 */
	public SignedIntegerRendering(String renderingId) {
		super(renderingId);
	}

	private String convertToString(byte[] byteArray, int columnSize, int endianess)
	{
		String ret;
		long result = 0;
		
		if (columnSize == 1)
		{
			result = byteArray[0];
		}
		else if (columnSize == 2)
		{	
			result = RenderingsUtil.convertByteArrayToShort(byteArray, endianess);
		}
		else if (columnSize == 4)
		{
			result = RenderingsUtil.convertByteArrayToInt(byteArray, endianess);
		}
		else if (columnSize == 8)
		{
			result = RenderingsUtil.convertByteArrayToLong(byteArray, endianess);				
		}
		else if (columnSize == 16)
		{
			BigInteger bigRet = RenderingsUtil.convertByteArrayToSignedBigInt(byteArray, endianess);
			return bigRet.toString();
		}
		else
		{
			BigInteger bigRet = RenderingsUtil.convertByteArrayToSignedBigInt(byteArray, endianess, columnSize);
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
				byte x = Byte.parseByte(newValue);
				bytes = new byte[1];
				bytes[0] = x;
			}
			else if (colSize == 2)
			{	
				short i = Short.parseShort(newValue);
				bytes = RenderingsUtil.convertShortToByteArray(i, endianess);
			}
			else if (colSize == 4)
			{	
				int i = Integer.parseInt(newValue);
				bytes = RenderingsUtil.convertIntToByteArray(i, endianess);
			}
			else if (colSize == 8)
			{	
				long i = Long.parseLong(newValue);
				bytes = RenderingsUtil.convertLongToByteArray(i, endianess);
			}
			else if (colSize == 16)
			{
				// special case for colSize == 16
				// need to represent number in Big Integer
				BigInteger i = new BigInteger(newValue);
				bytes = RenderingsUtil.convertBigIntegerToByteArray(i, endianess);
			
				return bytes;
			}		
			else
			{
				BigInteger i = new BigInteger(newValue);
				
				// avoid calculating max and min over and over again
				// for the same column size
				if (fColSize != colSize)
				{
					fColSize = colSize;
					fMax = BigInteger.valueOf(2);
					fMax = fMax.pow(colSize*8-1);
					fMin = fMax.multiply(BigInteger.valueOf(-1));
					fMax = fMax.subtract(BigInteger.valueOf(1));
				}
				
				if (i.compareTo(fMax) > 0 || i.compareTo(fMin) < 0)
					throw new NumberFormatException();
				
				bytes = RenderingsUtil.convertSignedBigIntToByteArray(i, endianess, colSize);
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
		
		boolean invalid = false;
		String paddedStr = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugUIConstants.PREF_PADDED_STR);
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
		
		// if the user has not set an endianess to the rendering
		// take default endianess from bytes
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
		
		// if the user has not set an endianess to the rendering
		// take default
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
}
