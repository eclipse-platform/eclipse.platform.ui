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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;


/**
 * A class to be optionally implemented by plugins that
 * wishes to create its own renderings.
 * 
 * If this class is not specified in the redering definition, a default
 * factory will be used and an instance of MemoryRendering will be created.
 * @since 3.1
 */
public interface IMemoryRenderingFactory {

	/**
	 * @param memoryBlock
	 * @param renderingId
	 * @return the rendering created.  Null if the rendering is not to be created.
	 * Throws a Debug Exception if an error has occurred.
	 */
	IMemoryRendering createRendering(IMemoryBlock memoryBlock, String renderingId) throws DebugException;
}
