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

package org.eclipse.debug.core.model;

import org.eclipse.debug.core.DebugException;


/**
 * Extended capabilites for memory block retrieval.
 * @since 3.1
 */
public interface IMemoryBlockRetrievalExtension extends IMemoryBlockRetrieval {
	
	/**
	 * Retrieves a memory block. 
	 * 
	 * @param expression - expression to be evalutated to an address, evaluation context can be retrieved
	 *                     from the selected debug element 
	 * @param context - context for evaluating the expression.  The is typically a debug element.
	 * @return a memory block based on the given expression and selected debug element
	 * @throws DebugException
	 */
	
	public IMemoryBlockExtension getExtendedMemoryBlock(String expression, Object context) throws DebugException;
}
