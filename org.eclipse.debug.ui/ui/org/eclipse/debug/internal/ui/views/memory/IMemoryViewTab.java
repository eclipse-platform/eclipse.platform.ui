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
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Font;


/**
 * Represent a view tab in the Memory View or Memory Rendering View
 * 
 * Refer to AbstractMemoryViewTab.
 * 
 * @since 3.0
 */
public interface IMemoryViewTab
{	
	/**
	 * @return the memory block blockied by the view tab
	 */
	public IMemoryBlock getMemoryBlock();
	
	/**
	 * Remove the view tab.  Memory block
	 * should not be removed from Memory Block Manager.
	 */
	public void dispose();
	
	/**
	 * This function makes the cursor to go to the address provided.  
	 * If the address provided is not currently displayed on screen, the view tab
	 * will ask for memory from the memory block to have that part of the memory displayed.  
	 * This function throws a DebugException if the call has failed.
	 * @param address
	 * @throws DebugException
	 */
	public void goToAddress(BigInteger address) throws DebugException;

	/**
	 * This function resets the view tab to the base address of the memory block.  
	 * It will throw a DebugException if the call has failed.
	 * @throws DebugException
	 */
	public void resetAtBaseAddress( ) throws DebugException;

	/**
	 * This function refreshes the view tab with the most current data from the memory block.  
	 * If the base address of the memory block has changed, it will reload at the new base address.  
	 * If the call has failed, it would display an error on the view tab.
	 */
	public void refresh( );

	/**
	 * @return true if the view tab is displaying an error, false otherwise.
	 */
	public boolean isDisplayingError();
	
	
	/**
	 * @return enablement state of the view tab.
	 */
	public boolean isEnabled();
	
	
	/**
	 * Sets the enablament state of the view tab.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);
	
	
	/**
	 * Sets the font to be used in a view tab.
	 * @param font
	 */
	public void setFont(Font font);
	
	/**
	 * Set view tab's label
	 * @param label
	 */
	public void setTabLabel(String label);
	
	/**
	 * @return view tab's label, null if the label is not available
	 */
	public String getTabLabel();
	
	/**
	 * @return rendering id of this view tab
	 */
	public String getRenderingId();
	
	/**
	 * @return the rendering of this view tab
	 */
	public IMemoryRendering getRendering();
	
	/**
	 * Allow view tab to supply its own context menu actions.
	 * Context menu extensions are hanled by the view.
	 *  
	 * @param menu
	 */
	public void fillContextMenu(IMenuManager menu);
	
	/**
	 * @return selected address in the view tab.
	 */
	public BigInteger getSelectedAddress();
	
	/**
	 * @return the content at the selected address.  View tab
	 * decides how long the content is and how much information
	 * to return.  Return empty string if content is not available.
	 */
	public String getSelectedContent();
}
