/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Pickens - [Memory View] Endian in hex view and ASCII view doesn't work
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;

import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * @since 3.3
 */
public class HexIntegerRendering extends AbstractIntegerRendering {
	
	public HexIntegerRendering(String renderingId)
	{
		super(renderingId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.AbstractTableRendering#getString(java.lang.String, java.math.BigInteger, org.eclipse.debug.core.model.MemoryByte[], java.lang.String)
	 */
	public String getString(String dataType, BigInteger address,
			MemoryByte[] data) {
		StringBuffer strBuffer = new StringBuffer();
		int endianess = getEndianness(data);

		String paddedStr = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugUIConstants.PREF_PADDED_STR);
		
        if (endianess == RenderingsUtil.LITTLE_ENDIAN) {
            MemoryByte[] swapped = new MemoryByte[data.length];
            for (int i = 0; i < data.length; i++){
                swapped[data.length-i-1] = data[i];
            }
            data = swapped;
        }
        
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

    /**
     * @todo davidp needs to add a method comment.
     * @param data
     * @return
     */
    private int getEndianness (MemoryByte[] data) {
        // if the user has not set an endianess to the rendering
        // take default
        int endianess = getDisplayEndianess();
        if (endianess == RenderingsUtil.ENDIANESS_UNKNOWN)
            endianess = getBytesEndianess(data);
        return endianess;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.AbstractTableRendering#getBytes(java.lang.String, java.math.BigInteger, org.eclipse.debug.core.model.MemoryByte[], java.lang.String)
	 */
	public byte[] getBytes(String dataType, BigInteger address,
			MemoryByte[] currentValues, String data) {

		int endianess = getEndianness(currentValues);
		byte[] bytes = RenderingsUtil.convertHexStringToByteArray(data, currentValues.length, getNumCharsPerByte());
		
		
        if (endianess == RenderingsUtil.LITTLE_ENDIAN) {
            byte[] swapped = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++){
                swapped[bytes.length-i-1] = bytes[i];
            }
           bytes = swapped;
        }
        
		return bytes;
	}
	
	public int getNumCharsPerByte()
	{
		return 2;
	}

	
}
