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
import org.eclipse.debug.core.model.IMemoryBlockExtension;

/**
 * Represents an integer rendering.
 * This allows plugins to contribute actions to the IntegerRendering only
 * via the ObjectContribution extension point.    
 */
public class IntegerRendering extends AbstractMemoryRendering{
	
	IMemoryBlock fMemoryBlock;
	String fRenderingId;
	
	private int fCurrentEndianess = RendererUtil.BIG_ENDIAN;
	
	public IntegerRendering(IMemoryBlock memBlock, String renderingId){
		super(memBlock, renderingId);
		
		if (memBlock instanceof IMemoryBlockExtension)
		{
			IMemoryBlockExtension exBlk = (IMemoryBlockExtension)memBlock;
			
			if(exBlk.isBigEndian()){
				fCurrentEndianess = RendererUtil.BIG_ENDIAN;
			}
			else
			{
				fCurrentEndianess = RendererUtil.LITTLE_ENDIAN;
			}
		}
	}
	/**
	 * @return Returns the currentEndianess.
	 */
	public int getCurrentEndianess() {
		return fCurrentEndianess;
	}

	/**
	 * @param currentEndianess The currentEndianess to set.
	 */
	public void setCurrentEndianess(int currentEndianess) {
		fCurrentEndianess = currentEndianess;
	}
}
