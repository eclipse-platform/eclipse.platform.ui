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
package org.eclipse.debug.ui.memory;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;


/**
 * Abstract implementation to a text rendering that renders memory to 
 * text in a table rendering.  Clients should subclass from this class
 * if they wish to provide a table/text rendering with a specific code
 * page.
 */
abstract public class AbstractTextRendering extends AbstractTableRendering { 	
	
	String fCodePage;
	
	public AbstractTextRendering(String renderingId, String codePage)
	{
		super(renderingId);
		setCodePage(codePage);
	}
	
	public void setCodePage(String codePage) {
		fCodePage = codePage;
	}

	public String getString(String dataType, BigInteger address,  MemoryByte[] data) {
		try {
			String paddedStr = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugPreferenceConstants.PREF_PADDED_STR);
			if(fCodePage == null)
				return ""; //$NON-NLS-1$
			
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
			
			byte byteArray[] = new byte[data.length];
			for (int i=0; i<byteArray.length; i++)
			{
				byteArray[i] = data[i].getValue(); 
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
