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


/**
 * Converts bytes into raw hex data and vice versa.
 * 
 * @since 3.0
 */
public class HexRenderer extends AbstractMemoryRenderer implements IFixedLengthOutputRenderer {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractMemoryRenderer#getString(java.lang.String, java.math.BigInteger, byte[])
	 */
	public String getString(
		String dataType,
		BigInteger address,
		byte[] data) {

		String str;
		str = convertByteArrayToHexString(data);
		str = str.toUpperCase();
		
		return str;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractMemoryRenderer#getBytes(java.lang.String, java.math.BigInteger, java.lang.String)
	 */
	public byte[] getBytes(
		String dataType,
		BigInteger address,
		byte[] currentValues, String data) {

		byte[] bytes = convertHexStringToByteArray(data);
		
		return bytes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.FixedLengthDataRenderer#getString(java.lang.String, java.math.BigInteger, byte[], int)
	 */
	public String getString(String renderingId, BigInteger address, byte[] data, int columnSize) {
		String str;
		str = convertByteArrayToHexString(data);
		str = str.toUpperCase();
		
		return str;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.FixedLengthDataRenderer#getNumCharPerByte()
	 */
	public int getNumCharPerByte() {
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
		char charArray[] = new char[2]; 
		
		for (int i=0; i<byteArray.length;i ++)
		{
			int val = byteArray[i];
			if (val < 0) val += 256;
			charArray[0] = Character.forDigit(val / 16, 16);
			charArray[1] = Character.forDigit(val % 16, 16);
			strBuffer.append(charArray);			
		}
		
		return strBuffer.toString();
	}

	/**
	 * Convert raw memory datat to byte array
	 * @param str
	 * @return
	 * @throws NumberFormatException
	 */
	public byte[] convertHexStringToByteArray(String str) throws NumberFormatException
	{
		if (str.length() < getNumCharPerByte())
			return null;
		
		byte[] bytes = new byte[str.length()/getNumCharPerByte()];
	
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
