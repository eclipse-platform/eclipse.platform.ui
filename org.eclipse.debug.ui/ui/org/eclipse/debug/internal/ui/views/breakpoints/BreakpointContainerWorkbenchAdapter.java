/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * BreakpointContainerWorkbenchAdapter
 */
public class BreakpointContainerWorkbenchAdapter implements IWorkbenchAdapter {

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
        if (object instanceof OrganizedBreakpointContainer) {
            OrganizedBreakpointContainer container = (OrganizedBreakpointContainer) object;
            IAdaptable category = container.getCategory();
            IWorkbenchAdapter adapter = (IWorkbenchAdapter) category.getAdapter(IWorkbenchAdapter.class);
            if (adapter != null) {
                return adapter.getImageDescriptor(category);
            }
            // TODO: this should really be handled by an IWorkingSet workbench adapater
            if (category instanceof IWorkingSet) {
                IWorkingSet set = (IWorkingSet) category;
                return set.getImage();
            }
            return container.getOrganizer().getImageDescriptor();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object object) {
        if (object instanceof OrganizedBreakpointContainer) {
            OrganizedBreakpointContainer container = (OrganizedBreakpointContainer) object;
            IAdaptable category = container.getCategory();
            IWorkbenchAdapter adapter = (IWorkbenchAdapter) category.getAdapter(IWorkbenchAdapter.class);
            if (adapter != null) {
                return adapter.getLabel(category);
            }
//          TODO: this should really be handled by an IWorkingSet workbench adapater
            if (category instanceof IWorkingSet) {
                IWorkingSet set = (IWorkingSet) category;
                return set.getName();
            }            
            return container.getOrganizer().getLabel();
        }
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        // not used
        return null;
    }

}
