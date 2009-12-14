/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Improve usability of the breakpoint view (Bug 238956)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;

/**
 * BreakpointContainerWorkbenchAdapter
 */
public class BreakpointContainerWorkbenchAdapter implements IWorkbenchAdapter, IWorkbenchAdapter2{

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        // not used
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        if (object instanceof IBreakpointContainer) {
            IBreakpointContainer container = (IBreakpointContainer) object;
            IAdaptable category = container.getCategory();
            if (category != null) {
	            IWorkbenchAdapter adapter = (IWorkbenchAdapter) category.getAdapter(IWorkbenchAdapter.class);
	            if (adapter != null) {
	                return adapter.getImageDescriptor(category);
	            }
	            return container.getOrganizer().getImageDescriptor();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object object) {
        if (object instanceof IBreakpointContainer) {
            IBreakpointContainer container = (IBreakpointContainer) object;
            IAdaptable category = container.getCategory();
            if (category != null) {
	            IWorkbenchAdapter adapter = (IWorkbenchAdapter) category.getAdapter(IWorkbenchAdapter.class);
	            if (adapter != null) {
	                return adapter.getLabel(category);
	            }
	            return container.getOrganizer().getLabel();
            }
        }
        return IInternalDebugCoreConstants.EMPTY_STRING;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getForeground(java.lang.Object)
     */
    public RGB getForeground(Object object) {
        if (object instanceof IBreakpointContainer) {
            IBreakpointContainer container = (IBreakpointContainer) object;
            IAdaptable category = container.getCategory();
            IWorkbenchAdapter2 adapter = (IWorkbenchAdapter2) category.getAdapter(IWorkbenchAdapter2.class);
            if (adapter != null) {
                return adapter.getForeground(category);
            }
        }        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getBackground(java.lang.Object)
     */
    public RGB getBackground(Object object) {
        if (object instanceof IBreakpointContainer) {
            IBreakpointContainer container = (IBreakpointContainer) object;
            IAdaptable category = container.getCategory();
            IWorkbenchAdapter2 adapter = (IWorkbenchAdapter2) category.getAdapter(IWorkbenchAdapter2.class);
            if (adapter != null) {
                return adapter.getBackground(category);
            }
        }        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter2#getFont(java.lang.Object)
     */
    public FontData getFont(Object object) {
        if (object instanceof IBreakpointContainer) {
            IBreakpointContainer container = (IBreakpointContainer) object;
            IAdaptable category = container.getCategory();
            IWorkbenchAdapter2 adapter = (IWorkbenchAdapter2) category.getAdapter(IWorkbenchAdapter2.class);
            if (adapter != null) {
                return adapter.getFont(category);
            }
        }        
        return null;
    }

}
