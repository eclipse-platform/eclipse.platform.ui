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
import org.eclipse.debug.internal.core.memory.IMemoryRendering;

/**
 * Abstract rendering class
 */
public abstract class AbstractMemoryRendering implements IMemoryRendering {

	private IMemoryBlock fMemoryBlock;
	private String fRenderingId;
	
	
	public AbstractMemoryRendering(IMemoryBlock memoryBlock, String renderingId){
		fMemoryBlock = memoryBlock;
		fRenderingId = renderingId;
	}
	
	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryRendering#getBlock()
	 */
	public IMemoryBlock getBlock() {
		return fMemoryBlock;
	}

	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryRendering#getRenderingId()
	 */
	public String getRenderingId() {
		return fRenderingId;
	}
}
