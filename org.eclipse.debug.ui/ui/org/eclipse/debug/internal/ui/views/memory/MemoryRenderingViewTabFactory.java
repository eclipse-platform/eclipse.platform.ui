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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.TabItem;

/**
 * Implementation of IMemoryViewTabFactory for default
 * renderings.
 * For plugins that wishes to make use of this factory to create a new rendering and display
 * data in table format, a renderer must be defined for the new rendering.
 * MemoryViewTab's label provider will make use of the renderer to convert bytes to string
 * and vice versa.  MemoryViewTab does not work without having a renderer defined.
 * 
 * @since 3.0
 */
public class MemoryRenderingViewTabFactory
	implements IMemoryViewTabFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IMemoryViewTabFactory#createViewTab(org.eclipse.debug.core.model.IMemoryBlock, org.eclipse.swt.widgets.TabItem, org.eclipse.jface.action.MenuManager, java.lang.String)
	 */
	public IMemoryViewTab createViewTab(
		IMemoryBlock newMemory,
		TabItem newTab,
		MenuManager menuMgr,
		IMemoryRendering rendering, AbstractMemoryRenderer renderer) {
		return new MemoryViewTab(newMemory, newTab, menuMgr, rendering, renderer);
	}

}
