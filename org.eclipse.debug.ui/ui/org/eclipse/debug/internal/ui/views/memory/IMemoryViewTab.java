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


package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.ui.memory.IMemoryRendering;


/**
 * Represent a view tab in the Memory View or Memory Rendering View
 * 
 * Refer to AbstractMemoryViewTab.
 * This is an internal interface. This class is not intended to be implemented by clients.
 * 
 * @since 3.0
 */
public interface IMemoryViewTab
{	
	/**
	 * Remove the view tab.
	 */
	public void dispose();
	
	/**
	 * @return if the view tab is disposed
	 */
	public boolean isDisposed();
	
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
	 * Set view tab's label
	 * @param label
	 */
	public void setTabLabel(String label);
	
	/**
	 * @return view tab's label, null if the label is not available
	 */
	public String getTabLabel();
	
	/**
	 * @return the rendering of this view tab
	 */
	public IMemoryRendering getRendering();
}
