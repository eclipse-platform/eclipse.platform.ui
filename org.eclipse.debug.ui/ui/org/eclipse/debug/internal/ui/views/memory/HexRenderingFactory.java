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
 * @since 3.0
 */
public class HexRenderingFactory implements IMemoryRenderingFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryRenderingFactory#createRendering(org.eclipse.debug.core.model.IMemoryBlock, java.lang.String)
	 */
	public IMemoryRendering createRendering(
		IMemoryBlock memoryBlock,
		String renderingId) {
		
		return new HexRendering(memoryBlock, renderingId);
	}

}
