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
import org.eclipse.debug.internal.core.memory.IMemoryRenderingFactory;

/**
 * Factory for creating the default renderings.
 * Renderings are created differently for different rendering
 * id so that Memory Rendering Pane's selection provider
 * can provide correct selections to allow objection contribution for pop
 * up menu actions.
 */
public class DefaultRenderingsFactory implements IMemoryRenderingFactory {

	public static final String RENDERING_ID_SIGNED_INT = "org.eclipse.debug.ui.rendering.signedint";
	public static final String RENDERING_ID_UNSIGNED_INT = "org.eclipse.debug.ui.rendering.unsignedint";
	public static final String RENDERING_ID_ASCII = "org.eclipse.debug.ui.rendering.ascii";
	
	/* (non-Javadoc)
	 * @see com.ibm.debug.extended.ui.IMemoryRenderingFactory#createRendering(org.eclipse.debug.core.model.IMemoryBlock, java.lang.String)
	 */
	public IMemoryRendering createRendering(
		IMemoryBlock memoryBlock,
		String renderingId) {
		
		if (renderingId.equals(RENDERING_ID_SIGNED_INT))
		{
			return new SignedIntegerRendering(memoryBlock, renderingId);
		}
		else if (renderingId.equals(RENDERING_ID_UNSIGNED_INT))
		{
			return new UnsignedIntegerRendering(memoryBlock, renderingId);
		}
		else if (renderingId.equals(RENDERING_ID_ASCII))
		{
			return new ASCIIRendering(memoryBlock, renderingId);
		}
		
		return null;
	}
}
