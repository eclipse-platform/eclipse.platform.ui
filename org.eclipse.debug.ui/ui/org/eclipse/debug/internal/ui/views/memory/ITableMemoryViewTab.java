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

/**
 * This interface is for capturing functions that are only applicable
 * for memory being displayed in table format.
 * 
 * @since 3.0
 */
public interface ITableMemoryViewTab extends IMemoryViewTab {

	/**
	 * View tab will be formatted based on the parameters provided.  
	 * Possible value for bytesPerLine is:  16*addressibleSize.  
	 * Although bytesPerLine is limited to 16, this function should still take this argument 
	 * for future extension when other values are to be supported.  
	 * Possible values for columnSize:  (1, 2, 4, 8, 16) * addressibleSize.
	 * @param bytesPerLine
	 * @param columnSize
	 * @return true if formatting is successful, false otherwise
	 */
	public boolean format (int bytesPerLine, int columnSize);
	
	/**
	 * @return number of bytes per line from the view tab.
	 */
	public int getBytesPerLine();
	
	/**
	 * @return number of addressible unit per line
	 */
	public int getAddressibleUnitPerLine();
	
	/**
	 * @return number of bytes per column from the view tab.
	 */
	public int getBytesPerColumn();
	
	/**
	 * @return number of addressible unit per column
	 */
	public int getAddressibleUnitPerColumn();
	
	/**
	 * @return the size of the smallest addressible unit in bytes
	 */
	public int getAddressibleSize();
	
	
	/**
	 * @return number of visible lines from the view tab.
	 */
	public int getNumberOfVisibleLines();
	
	/**
	 * Set view tab to show/hide its address column
	 * @param showColumn
	 */
	public void showAddressColumn(boolean showColumn);
	
	/**
	 * @return if address column is currently shown
	 */
	public boolean isShowAddressColumn();
	
}
