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
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * @since 3.3
 */
public class DebugTargetContentProvider extends ElementContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildCount(java.lang.Object, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected int getChildCount(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		String id = context.getId();
		if (id.equals(IDebugUIConstants.ID_DEBUG_VIEW))
		{
			return ((IDebugTarget)element).getThreads().length;
		}
		else if (id.equals(IDebugUIConstants.ID_MEMORY_VIEW))
		{
			return getAllChildren(element, context, monitor).length;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#supportsContextId(java.lang.String)
	 */
	protected boolean supportsContextId(String id) {
		return IDebugUIConstants.ID_DEBUG_VIEW.equals(id) || IDebugUIConstants.ID_MEMORY_VIEW.equals(id);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.elements.ElementContentProvider#getChildren(java.lang.Object, int, int, org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext)
	 */
	protected Object[] getChildren(Object parent, int index, int length, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		return getElements(getAllChildren(parent, context, monitor), index, length);
	}

	protected boolean hasChildren(Object element, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		String id = context.getId();
		if (id.equals(IDebugUIConstants.ID_DEBUG_VIEW))
		{
			return ((IDebugTarget)element).hasThreads();
		}
		else if (id.equals(IDebugUIConstants.ID_MEMORY_VIEW))
		{
			return getAllChildren(element, context, monitor).length > 0;
		}
		return false;
	}

	/**
	 * Returns all children of the given parent object.
	 * 
	 * @param parent 
	 * @param context
	 * @param monitor
	 * @return all children
	 * @throws CoreException
	 */
	protected Object[] getAllChildren(Object parent, IPresentationContext context, IViewerUpdate monitor) throws CoreException {
		String id = context.getId();
		if (id.equals(IDebugUIConstants.ID_DEBUG_VIEW))
		{
			return ((IDebugTarget)parent).getThreads();
		}
		else if (id.equals(IDebugUIConstants.ID_MEMORY_VIEW))
        {
			if (parent instanceof IMemoryBlockRetrieval)
			{
				if (((IMemoryBlockRetrieval)parent).supportsStorageRetrieval())
        			return DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks((IMemoryBlockRetrieval)parent);
			}
        }
        return EMPTY;
	}
	
	
}
