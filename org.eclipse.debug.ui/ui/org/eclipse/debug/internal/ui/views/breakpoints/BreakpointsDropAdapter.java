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

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * BreakpointsDropAdapter
 */
public class BreakpointsDropAdapter extends ViewerDropAdapter {
    
    private BreakpointContainer fContainer = null;

    /**
     * @param viewer
     */
    protected BreakpointsDropAdapter(Viewer viewer) {
        super(viewer);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    public boolean performDrop(Object data) {
        if (fContainer != null && data instanceof IStructuredSelection) {
            Object[] objects = ((IStructuredSelection)data).toArray();
            for (int i = 0; i < objects.length; i++) {
                fContainer.getOrganizer().addBreakpoint((IBreakpoint)objects[i], fContainer.getCategory());
            }
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        fContainer = null;
        if (target instanceof BreakpointContainer) {
            BreakpointContainer container = (BreakpointContainer) target;
            ISelection selection = LocalSelectionTransfer.getInstance().getSelection();
            if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
                Object[] objects = ((IStructuredSelection)selection).toArray();
                for (int i = 0; i < objects.length; i++) {
                    if (objects[i] instanceof IBreakpoint) {
                        IBreakpoint breakpoint = (IBreakpoint)objects[i];
                        if (container.contains(breakpoint) || !container.getOrganizer().canAdd(breakpoint, container.getCategory())) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                fContainer = container;
                return true;
            }
        }
        return false;
    }

}
