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

import org.eclipse.debug.core.model.IMemoryBlock;


/**
 * This interface should be implemented by views that display 
 * a memory block and wish to be synchronized with the Memory View
 * 
 * @since 3.0
 */
public interface ISynchronizedMemoryBlockView
{
	/**
	 * @return the memory block that this view tab is blocking.
	 */
	public IMemoryBlock getMemoryBlock();
	
	/**
	 * This function will be called when a property of the memory block
	 * being blocked is changed.
	 * @param propertyId
	 * @param value
	 */
	public void propertyChanged(String propertyId, Object value);
	
	/**
	 * Return the value of a property.  Return null if the property
	 * is not supported by the view tab.
	 * @param propertyId
	 * @return the value of a property
	 */
	public Object getProperty(String propertyId);
	
	/**
	 * @return true if the view is enabled
	 */
	public boolean isEnabled();
}
