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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

public class BreakpointsDragAdapter extends DragSourceAdapter implements TransferDragSourceListener {
    
    private ISelectionProvider fProvider;
    private BreakpointsContentProvider fContentProvider;
    private List fContainers = new ArrayList();
    
    public BreakpointsDragAdapter(BreakpointsContentProvider content, ISelectionProvider provider) {
        Assert.isNotNull(provider);
        fProvider= provider;
        fContentProvider = content;
    }

    /**
     * @see TransferDragSourceListener#getTransfer
     */
    public Transfer getTransfer() {
        return LocalSelectionTransfer.getInstance();
    }
    
    /* non Java-doc
     * @see org.eclipse.swt.dnd.DragSourceListener#dragStart
     */
    public void dragStart(DragSourceEvent event) {
        ISelection selection= fProvider.getSelection();
        LocalSelectionTransfer.getInstance().setSelection(selection);
        LocalSelectionTransfer.getInstance().setSelectionSetTime(event.time & 0xFFFFFFFFL);
        event.doit= isDragable(selection);
    }
    
    /**
     * Checks if the elements contained in the given selection can
     * be dragged.
     * <p>
     * Subclasses may override.
     * 
     * @param selection containing the elements to be dragged
     */
    protected boolean isDragable(ISelection selection) {
        fContainers.clear();
        if (selection.isEmpty() || !fContentProvider.isShowingGroups()) {
            return false;
        }
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object[] objects = ss.toArray();
            for (int i = 0; i < objects.length; i++) {
                Object object = objects[i];
                if (object instanceof IBreakpoint) {
                    IBreakpoint breakpoint = (IBreakpoint) object;
                    BreakpointContainer[] containers = fContentProvider.getLeafContainers(breakpoint);
                    if (containers != null) {
                        for (int j = 0; j < containers.length; j++) {
                            BreakpointContainer container = containers[j];
                            if (container.getOrganizer().canRemove(breakpoint, container.getCategory())) {
                                fContainers.add(container);
                            } else {
                                return false;
                            }
                        }
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }


    /* non Java-doc
     * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData
     */     
    public void dragSetData(DragSourceEvent event) {
        // For consistency set the data to the selection even though
        // the selection is provided by the LocalSelectionTransfer
        // to the drop target adapter.
        event.data= LocalSelectionTransfer.getInstance().getSelection();
    }


    /* non Java-doc
     * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished
     */ 
    public void dragFinished(DragSourceEvent event) {
        if (event.detail != DND.DROP_NONE) {
            IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getInstance().getSelection();
            Object[] objects = selection.toArray();
            Iterator iterator = fContainers.iterator();
            while (iterator.hasNext()) {
                BreakpointContainer container = (BreakpointContainer) iterator.next();
                for (int i = 0; i < objects.length; i++) {
                    IBreakpoint breakpoint = (IBreakpoint) objects[i];
                    container.getOrganizer().removeBreakpoint(breakpoint, container.getCategory());
                }
            }
        }
        LocalSelectionTransfer.getInstance().setSelection(null);
        LocalSelectionTransfer.getInstance().setSelectionSetTime(0);
        fContainers.clear();
    }   
}
