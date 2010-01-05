/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.model.elements;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;

public class MemoryRetrievalContentProvider extends ElementContentProvider {

	protected int getChildCount(Object element, IPresentationContext context,
			IViewerUpdate monitor) throws CoreException {
		return getAllChildren(element, context, monitor).length;
	}

	protected Object[] getChildren(Object parent, int index, int length,
			IPresentationContext context, IViewerUpdate monitor)
			throws CoreException {
		
		return getElements(getAllChildren(parent, context, monitor), index, length);
		
	}
	
	/**
	 * Returns all children of the given parent in the given context.
	 * 
	 * @param parent
	 * @param context
	 * @param monitor
	 * @return all children
	 */
	protected Object[] getAllChildren(Object parent, IPresentationContext context, IViewerUpdate monitor) {
		String id = context.getId();
		if (id.equals(IDebugUIConstants.ID_MEMORY_VIEW))
        {
			if (parent instanceof IMemoryBlockRetrieval)
			{
				if (((IMemoryBlockRetrieval)parent).supportsStorageRetrieval())
        			return DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks((IMemoryBlockRetrieval)parent);
			}
        }
        return EMPTY;
	}

	protected boolean supportsContextId(String id) {
		return id.equals(IDebugUIConstants.ID_MEMORY_VIEW);
	}

}
