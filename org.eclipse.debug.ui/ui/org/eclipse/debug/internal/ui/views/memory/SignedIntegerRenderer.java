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

import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.core.memory.IMemoryRendering;
import org.eclipse.debug.internal.core.memory.MemoryBlockManager;
import org.eclipse.debug.internal.core.memory.MemoryByte;

/**
 * Converts bytes to signed integer and vice versa
 */
public class SignedIntegerRenderer
	extends AbstractMemoryRenderer {
	
	private ITableMemoryViewTab fTableViewTab;
	
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
		}
		else if (columnSize == 2)
		{	
			result = RendererUtil.convertByteArrayToShort(byteArray, endianess);
		}
		else if (columnSize == 4)
		{
			result = RendererUtil.convertByteArrayToInt(byteArray, endianess);
		}
		else if (columnSize == 8)
		{
			result = RendererUtil.convertByteArrayToLong(byteArray, endianess);				
		}
		else
		{
			BigInteger bigRet = RendererUtil.convertByteArrayToBigInteger(byteArray, endianess);
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
				bytes = RendererUtil.convertShortToByteArray(i, endianess);
			}
			else if (colSize == 4)
			{	
				int i = Integer.parseInt(newValue);
				bytes = RendererUtil.convertIntToByteArray(i, endianess);
			}
			else if (colSize == 8)
			{	
				long i = Long.parseLong(newValue);
				bytes = RendererUtil.convertLongToByteArray(i, endianess);
			}
			else
			{
				// special case for colSize == 16
				// need to represent number in Big Integer
				BigInteger i = new BigInteger(newValue);
				bytes = RendererUtil.convertBigIntegerToByteArray(i, endianess);
			
				return bytes;
			}		
			
			return bytes;
		} catch (NumberFormatException e) {
			throw e;
		}
	}
	
	/**
	 * @return
	 */
	private int getEndianess() {
		
		IMemoryBlock memBlk = fViewTab.getMemoryBlock();
		
		// default to Big Endian in case the endianess cannot be determined
		int endianess = RendererUtil.BIG_ENDIAN;

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
			if ((data[i].flags & MemoryByte.VALID) == 0)
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
				byteArray[i] = data[i].value;
			}
			
			return convertToString(byteArray, columnSize, endianess);
		}
		else
			return "";
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
		else
			return new byte[0];
	}

}
