/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 	   Wind River - Pawel Piech - Initial Implementation - Drag/Drop to Expressions View (Bug 184057)
 *     IBM Corporation - further implementation and documentation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;

/**
 * Drag adapter for the variables view and expressions view.  Allows selected variables and
 * expressions to be dragged.
 * 
 * @see org.eclipse.debug.internal.ui.views.expression.ExpressionDropAdapter
 * @since 3.4
 */
public class SelectionDragAdapter extends DragSourceAdapter implements TransferDragSourceListener {
    
    /**
     * The associated viewer for the adapter
     */
    private TreeModelViewer fViewer;
    
    /**
     * Constructor, takes the viewer that contains the selection provider 
     * @param view the associated view, <b>must</b> implement <code>ISelectionProvider</code>
     */
    public SelectionDragAdapter(TreeModelViewer viewer) {
        Assert.isNotNull(viewer);
        fViewer = viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.TransferDragSourceListener#getTransfer()
     */
    public Transfer getTransfer() {
        return LocalSelectionTransfer.getTransfer();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
     */
    public void dragStart(DragSourceEvent event) {
        ISelection selection = fViewer.getSelection();
        LocalSelectionTransfer.getTransfer().setSelection(selection);
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & 0xFFFFFFFFL);
        event.doit = !selection.isEmpty();
    }
   
    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
     */
    public void dragSetData(DragSourceEvent event) {
        // For consistency set the data to the selection even though
        // the selection is provided by the LocalSelectionTransfer
        // to the drop target adapter.
        event.data = LocalSelectionTransfer.getTransfer().getSelection();
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceAdapter#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
     */
    public void dragFinished(DragSourceEvent event) {
        LocalSelectionTransfer.getTransfer().setSelection(null);
        LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);
    }   
}
