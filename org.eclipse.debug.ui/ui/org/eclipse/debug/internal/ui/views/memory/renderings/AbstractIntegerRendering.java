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
package org.eclipse.debug.internal.ui.views.memory.renderings;

import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;

/**
 * Abstract implementation to an integer rendering.
 * @since 3.1
 *
 */
public abstract class AbstractIntegerRendering extends AbstractTableRendering {
	
	private int fCurrentEndianess = RenderingsUtil.BIG_ENDIAN;
	
	public AbstractIntegerRendering(String renderingId){
		super(renderingId);
	}
	
	public void init(IMemoryRenderingContainer container, IMemoryBlock block) {
		super.init(container, block);
		
		if (getMemoryBlock() instanceof IMemoryBlockExtension)
		{
			IMemoryBlockExtension exBlk = (IMemoryBlockExtension)getMemoryBlock();
			
			if(exBlk.isBigEndian()){
				fCurrentEndianess = RenderingsUtil.BIG_ENDIAN;
			}
			else
			{
				fCurrentEndianess = RenderingsUtil.LITTLE_ENDIAN;
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
