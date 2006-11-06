/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.TransferDragSourceListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * A drag adapter for the breakpoints viewer
 */
public class BreakpointsDragAdapter extends DragSourceAdapter implements TransferDragSourceListener {
    
    /**
     * the associated viewer for the adapter
     */
    private BreakpointsViewer fViewer;
    private Item[] fItems = null;
    
    /**
     * Constructor
     * @param view the associiated view, which acts as the selection provider and therefore <b>must</b> implement <code>ISelectionProvider</code>
     */
    public BreakpointsDragAdapter(BreakpointsViewer viewer) {
        Assert.isNotNull(viewer);
        fViewer = viewer;
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
        ISelection selection = fViewer.getSelection();
        LocalSelectionTransfer.getInstance().setSelection(selection);
        LocalSelectionTransfer.getInstance().setSelectionSetTime(event.time & 0xFFFFFFFFL);
        event.doit = fViewer.canDrag(fViewer.getSelectedItems());
        fItems = fViewer.getSelectedItems();
    }
   
    /* non Java-doc
     * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData
     */     
    public void dragSetData(DragSourceEvent event) {
        // For consistency set the data to the selection even though
        // the selection is provided by the LocalSelectionTransfer
        // to the drop target adapter.
        event.data = LocalSelectionTransfer.getInstance().getSelection();
    }

    /* non Java-doc
     * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished
     */ 
    public void dragFinished(DragSourceEvent event) {
        if (event.detail == DND.DROP_MOVE) {
            // remove from source on move operation
        	fViewer.performDrag(fItems);
        }
        fItems = null;
        LocalSelectionTransfer.getInstance().setSelection(null);
        LocalSelectionTransfer.getInstance().setSelectionSetTime(0);
    }   
}
