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
 * Class responsible for creating an IMemoryViewTab based on the given parameters.
 * 
 * @since 3.0
 */
public interface IMemoryViewTabFactory {

	/**
	 * Create a view tab based on the given parameters.
	 * @param newMemory is the memory block to be blocked by this memory view tab.
	 * The view tab is responsible for blocking changes from the memory block and refreshing
	 * its content accordingly.
	 * @param newTab  is the tab item in the Memory View or Memory Rendering View
	 * used to display this view tab.  Implementor needs to set the created memory view
	 * tab as the data in the tab item.  (i.e.  call newTab.setData(memory view tab))
	 * @param menuMgr is the menuger manager the view tab should used in creation of
	 * its context menu.
	 * @param rendering is the rendering to be displayed by the view tab
	 * @param renderer is the object responsible for converting bytes to string and vice versa.  This field
	 * is optional and can be null.
	 * @return the memory view tab created, null if the view tab cannot be created
	 */
	public IMemoryViewTab createViewTab(IMemoryBlock newMemory, TabItem newTab, MenuManager menuMgr, IMemoryRendering rendering, AbstractMemoryRenderer renderer);
	
}
