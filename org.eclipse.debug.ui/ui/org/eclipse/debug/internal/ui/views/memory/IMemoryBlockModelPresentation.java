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
import org.eclipse.debug.ui.IDebugModelPresentation;


/**
 * Allows plugins to control the presentation of a memory block.  Plugins can
 * customize the tab label of a MemoryViewTab.  They can also control how 
 * each of the columns are labeled and how addresses are to be presented to users.
 * 
 * @since 3.0
 */
public interface IMemoryBlockModelPresentation extends IDebugModelPresentation
{
	/**
	 * @param blk - memory block the tab is monitoring
	 * @param renderingId - rendering id of the tab
	 * @return tab label for this memory block.  Return null if default is to be used.
	 * Default tab label is:
	 *   "expression:evaluated address" for IMemoryBlockExtension
	 *   "Base Address"  for IMemoryBlock
	 */
	public String getTabLabel(IMemoryBlock blk, String renderingId);
	
	/**
	 * @param blk
	 * @param bytesPerLine
	 * @param columnSize
	 * @return column labels for the MemoryViewTab of the memory block
	 * Size of the String array returned must equal to "bytesPerLine"/"columnSize".
	 * Return an empty array if default column labels are to be used.
	 */
	public String[] getColumnLabels(IMemoryBlock blk, int bytesPerLine, int columnSize);
	
	
	/**
	 * @param blk
	 * @param address
	 * @return the address presentation of the specfied address.
	 * Return null if default address presentation is to be used.
	 */
	public String getAddressPresentation(IMemoryBlock blk, BigInteger address);
}
