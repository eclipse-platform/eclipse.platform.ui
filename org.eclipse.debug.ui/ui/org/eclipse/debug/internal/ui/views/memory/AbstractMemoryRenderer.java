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
 * A rederer is a class that is responsible to convert data in byte
 * into a string and vice versa.
 * 
 * @since 3.0
 */
abstract public class AbstractMemoryRenderer {
	
	protected IMemoryViewTab fViewTab;
	protected String fRenderingId;
	
	/**
	 * Sets the view tab of which the renderer is rendering for.
	 * @param viewTab
	 */
	public void setViewTab(IMemoryViewTab viewTab){
		fViewTab = viewTab;
	}
	
	/**
	 * Sets the rendering id of this renderer.
	 * @param rederingId
	 */
	public void setRenderingId(String rederingId){
		fRenderingId = rederingId;
	}
	
	/**
	 * This is called by the label provider for IMemoryViewTab
	 * Implementor can reuse a memory view tab and presents data in a different format.
	 * @param dataType - type of data the bytes hold
	 * @param address - addres where the bytes belong to
	 * @param data - the bytes
	 * @param paddedStr - fill each byte that is invalid with this padded string.
	 * @return a string to represent the memory.  
	 * Do not return null.  Return a string to pad the cell if the memory cannot be converted successfully.
	 */
	abstract public String getString(String dataType, BigInteger address, MemoryByte[] data, String paddedStr);
	
	/**
	 * This is called by the cell modifier from an IMemoryViewTab.
	 * Implementor will convert the string value to an array of bytes.  The bytes will
	 * be passed to the debug adapter for memory block modification.
	 * Return null if the byte cannot be formatted properly.
	 * @param dataType - type of data the string represents
	 * @param address - address where the bytes belong to
	 * @param currentValues - current values of the data in bytes format
	 * @param data - the string to be converted to bytes
	 * @return the bytes to be passed to debug adapter for modification.
	 */
	abstract public byte[] getBytes(String dataType, BigInteger address, MemoryByte[] currentValues, String data);

}
