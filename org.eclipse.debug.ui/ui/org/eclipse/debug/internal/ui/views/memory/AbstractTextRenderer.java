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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.eclipse.debug.internal.core.memory.MemoryByte;


/**
 * Default implementation of a text renderer.
 * This renderer takes a code page and converts bytes into string in the specified code page.
 */
abstract public class AbstractTextRenderer extends AbstractMemoryRenderer implements IFixedLengthOutputRenderer {
	
	
	protected class RendererMemoryByte extends MemoryByte
	{
		
	}
	
	String fCodePage;
	
	public void setCodePage(String codePage){ 
		fCodePage = codePage;
	}

	public String getString(String dataType, BigInteger address,  MemoryByte[] data, String paddedStr) {
		try {
			
			if(fCodePage == null)
				return ""; //$NON-NLS-1$
			
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
			
			byte byteArray[] = new byte[data.length];
			for (int i=0; i<byteArray.length; i++)
			{
				byteArray[i] = data[i].value; 
			}

			return new String(byteArray, fCodePage);

		} catch (UnsupportedEncodingException e) {
			return "-- error --"; //$NON-NLS-1$
		}
	}
	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.AbstractTableViewTabLabelProvider#getBytes(java.lang.String)
	 */
	public byte[] getBytes(String dataType, BigInteger address, MemoryByte[] currentValues, String data) {
		try {
			
			if (fCodePage == null)
				return new byte[0];
			
			byte[] bytes =  data.getBytes(fCodePage);
			return bytes;
			
		} catch (UnsupportedEncodingException e) {
			return new byte[0];
		}
	}
}
