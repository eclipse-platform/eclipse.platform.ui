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

package org.eclipse.debug.internal.core.memory;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;


/**
 * Extended capabilites for memory block retrieval.
 * @since 3.0
 */
public interface IExtendedMemoryBlockRetrieval extends IMemoryBlockRetrieval {
	
	/**
	 * Retrieves a memory block. 
	 * 
	 * @param expression - expression to be evalutated to an address, evaluation context can be retrieved
	 *                     from the selected debug element 
	 * @param selected - selected debug element from Debug View
	 * @return
	 * @throws DebugException
	 */
	
	public IExtendedMemoryBlock getExtendedMemoryBlock(String expression, IDebugElement selected) throws DebugException;

	/**
	 * @return the string to be used in place of this memory content when bytes
	 * are not available.
	 * Return null if the byte is available.
	 */
	public String getPaddedString();

}
