/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ken Dyck - Bug 90154: [Memory View] Short Input Rejected by Hex Rendering
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;

import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractTableRendering;

/**
 * @since 3.1
 */
public class HexRendering extends AbstractTableRendering {
	
	public HexRendering(String renderingId)
	{
		super(renderingId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.AbstractTableRendering#getString(java.lang.String, java.math.BigInteger, org.eclipse.debug.core.model.MemoryByte[], java.lang.String)
	 */
	public String getString(String dataType, BigInteger address,
			MemoryByte[] data) {
		StringBuffer strBuffer = new StringBuffer();

		String paddedStr = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugUIConstants.PREF_PADDED_STR);
		
		for (int i=0; i<data.length; i++)
		{
			if (data[i].isReadable())
			{
				strBuffer.append(new String(convertByteToCharArray(data[i].getValue())));
			}
			else
			{
				// pad with padded string
				strBuffer.append(paddedStr);
			}
		}
		
		return strBuffer.toString().toUpperCase();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.AbstractTableRendering#getBytes(java.lang.String, java.math.BigInteger, org.eclipse.debug.core.model.MemoryByte[], java.lang.String)
	 */
	public byte[] getBytes(String dataType, BigInteger address,
			MemoryByte[] currentValues, String data) {
		byte[] bytes = convertHexStringToByteArray(data, currentValues.length);
		
		return bytes;
	}
	
	public int getNumCharsPerByte()
	{
		return 2;
	}

	/**
	 * byte array to Hex string helper
	 * replaces the Integer.toHexString() which can't convert byte values properly
	 * (always pads with FFFFFF)
	 */
	static public String convertByteArrayToHexString(byte[] byteArray)
	{
		StringBuffer strBuffer = new StringBuffer();
		char charArray[];
		
		for (int i=0; i<byteArray.length;i ++)
		{
			charArray = convertByteToCharArray(byteArray[i]);
			strBuffer.append(charArray);			
		}
		
		return strBuffer.toString();
	}
	
	static private char[] convertByteToCharArray(byte aByte)
	{
		char charArray[] = new char[2];
		int val = aByte;
		if (val<0) val += 256;
		charArray[0] = Character.forDigit(val/16, 16);
		charArray[1] = Character.forDigit(val%16, 16);
		
		return charArray;
	}

	/**
	 * Convert raw memory data to byte array
	 * @param str
     * @param numBytes
	 * @return an array of byte, converted from a hex string
	 * @throws NumberFormatException
	 */
	public byte[] convertHexStringToByteArray(String str, int numBytes) throws NumberFormatException
	{
        if (str.length() == 0) 
            return null;
		
		StringBuffer buf = new StringBuffer(str);
        
        // pad string with zeros
        int requiredPadding =  numBytes * getNumCharsPerByte() - str.length();
        while (requiredPadding > 0) {
            buf.insert(0, "0"); //$NON-NLS-1$
            requiredPadding--;
        }
		
		byte[] bytes = new byte[numBytes];
		str = buf.toString();
	
		// set data in memory
		for (int i=0; i<bytes.length; i++)
		{
			// convert string to byte
			String oneByte = str.substring(i*2, i*2+2);
			
			Integer number = Integer.valueOf(oneByte, 16);
			if (number.compareTo(Integer.valueOf(Byte.toString(Byte.MAX_VALUE))) > 0)
			{
				int temp = number.intValue();
				temp = temp - 256;
	
				String tempStr = Integer.toString(temp);
		
				Byte myByte = Byte.valueOf(tempStr);
				bytes[i] = myByte.byteValue();
			}
			else
			{
				Byte myByte = Byte.valueOf(oneByte, 16);
				bytes[i] = myByte.byteValue();
			}
		}
		
		return bytes;
	}
	
}
