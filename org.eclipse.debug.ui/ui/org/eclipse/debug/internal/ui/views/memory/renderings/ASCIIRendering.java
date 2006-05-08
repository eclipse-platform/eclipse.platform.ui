/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.memory.provisional.AbstractAsyncTextRendering;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.util.PropertyChangeEvent;




/**
 * Convert bytes into ASCII string and vice versa
 * @since 3.1
 */
public class ASCIIRendering extends AbstractAsyncTextRendering{
	
	private final int numCharsPerByte = 1;
	

	public ASCIIRendering(String renderingId)
	{
		super(renderingId);
		String codepage = DebugUITools.getPreferenceStore().getString(IDebugUIConstants.PREF_DEFAULT_ASCII_CODE_PAGE);
		setCodePage(codepage);
	}
	
	public void dispose() {
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.AbstractMemoryRendering#getNumCharsPerByte()
	 */
	public int getNumCharsPerByte() {
		return numCharsPerByte;
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.AbstractTableRendering#getBytes(java.lang.String, java.math.BigInteger, org.eclipse.debug.core.model.MemoryByte[], java.lang.String)
	 */
	public byte[] getBytes(
		String renderingId,
		BigInteger address,
		MemoryByte[] currentValues,
		String data) {
		
		byte[] bytes =  super.getBytes(renderingId, address, currentValues, data);
		
		// undo the replacement of 1's to 0's.
		for (int i=0; i<bytes.length && i < currentValues.length; i++)
		{
			if (bytes[i] == 1 && currentValues[i].getValue() == 0)
			{
				bytes[i] = 0;
			}
		}
		
		return bytes;
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.AbstractTableRendering#getString(java.lang.String, java.math.BigInteger, org.eclipse.debug.core.model.MemoryByte[])
	 */
	public String getString(
		String renderingId,
		BigInteger address,
		MemoryByte[] data) {
		
		MemoryByte[] copy = new MemoryByte[data.length];

		// If a byte equals zero, it represents null in a string
		// and often causes subsequent string not displayed or printed properly
		// Replace all null with 1's
		for (int i=0; i<data.length; i++){
			copy[i] = new MemoryByte();
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
		
		return super.getString(renderingId, address, copy);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		
		// handle code page changed event
		if (event.getProperty().equals(IDebugUIConstants.PREF_DEFAULT_ASCII_CODE_PAGE))
		{
			String codePage = (String)event.getNewValue();
			setCodePage(codePage);
			
			if (isVisible())
				// just update labels, don't need to reget memory
				updateLabels();
		}
		
		super.propertyChange(event);
	}
}
