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



/**
 * Convert bytes into ASCII string and vice versa
 */
public class ASCIIRenderer extends AbstractTextRenderer{
	
	private final static String ASCII_CODE_PAGE = "Cp1252"; //$NON-NLS-1$
	private final int numCharsPerByte = 1;
	

	public ASCIIRenderer()
	{
		setCodePage(ASCII_CODE_PAGE);
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.FixedLengthDataRenderer#getNumCharPerByte()
	 */
	public int getNumCharPerByte() {
		return numCharsPerByte;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.AbstractMemoryRenderer#getBytes(java.lang.String, java.math.BigInteger, byte[], java.lang.String)
	 */
	public byte[] getBytes(
		String renderingId,
		BigInteger address,
		MemoryByte[] currentValues,
		String data) {
		
		byte[] bytes =  super.getBytes(renderingId, address, currentValues, data);
		
		// undo the replacement of 1's to 0's.
		for (int i=0; i<bytes.length; i++)
		{
			if (bytes[i] == 1 && currentValues[i].getValue() == 0)
			{
				bytes[i] = 0;
			}
		}
		
		return bytes;
		
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.AbstractMemoryRenderer#getString(java.lang.String, java.math.BigInteger, byte[])
	 */
	public String getString(
		String renderingId,
		BigInteger address,
		MemoryByte[] data, String paddedStr) {
		
		MemoryByte[] copy = new MemoryByte[data.length];

		// If a byte equals zero, it represents null in a string
		// and often causes subsequent string not displayed or printed properly
		// Replace all null with 1's
		for (int i=0; i<data.length; i++){
			copy[i] = new RendererMemoryByte();
			if (data[i].getValue() == 0)
			{
				copy[i].setValue((byte)1);
			}
			else
			{
				copy[i].setValue(data[i].getValue());
			}
			copy[i].setFlags(data[i].getFlags());
		}
		
		return super.getString(renderingId, address, copy, paddedStr);
	}
}
