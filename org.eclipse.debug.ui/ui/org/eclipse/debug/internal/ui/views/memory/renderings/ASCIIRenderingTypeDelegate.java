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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingTypeDelegate;

/**
 * The delegate for creating an ASCII rendering.
 * @since 3.1
 */
public class ASCIIRenderingTypeDelegate implements IMemoryRenderingTypeDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.memory.IMemoryRenderingTypeDelegate#createRendering(java.lang.String)
	 */
	public IMemoryRendering createRendering(String id) throws CoreException {
		return new ASCIIRendering(id);
	}

}
