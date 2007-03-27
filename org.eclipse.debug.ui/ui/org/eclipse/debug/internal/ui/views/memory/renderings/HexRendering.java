/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.memory.provisional.AbstractAsyncTableRendering;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * @since 3.1
 */
public class HexRendering extends AbstractAsyncTableRendering {
	
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
				strBuffer.append(new String(RenderingsUtil.convertByteToCharArray(data[i].getValue())));
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
		byte[] bytes = RenderingsUtil.convertHexStringToByteArray(data, currentValues.length, getNumCharsPerByte());
		
		return bytes;
	}
	
	public int getNumCharsPerByte()
	{
		return 2;
	}
	
}
