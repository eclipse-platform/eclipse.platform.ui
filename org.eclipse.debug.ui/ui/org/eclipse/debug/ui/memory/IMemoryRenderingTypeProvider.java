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
package org.eclipse.debug.ui.memory;

import org.eclipse.debug.core.model.IMemoryBlock;


/**
 * A rendering type provider allows for dynamic rendering type bindings for a memory block,
 * and is contributed via memory block type binding.
 * 
 * @since 3.1
 */
public interface IMemoryRenderingTypeProvider {
	
	/**
     * Retunrs the rendering types applicable to the given memory block.
     * 
	 * @return a list of rendering types applicable to the given memory block
	 */
	IMemoryRenderingType [] getRenderingTypes(IMemoryBlock block);
}
