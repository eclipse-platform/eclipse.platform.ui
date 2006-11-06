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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * BreakpointsDropAdapter
 */
public class BreakpointsDropAdapter extends ViewerDropAdapter {

	private Item fTarget = null;
	
    /**
     * @param viewer
     */
    protected BreakpointsDropAdapter(BreakpointsViewer viewer) {
        super(viewer);
        setFeedbackEnabled(false);
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    public boolean performDrop(Object data) {
    	return ((BreakpointsViewer)getViewer()).performDrop(fTarget, (IStructuredSelection) LocalSelectionTransfer.getInstance().getSelection());
    }

	/**
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#determineTarget(org.eclipse.swt.dnd.DropTargetEvent)
	 */
	protected Object determineTarget(DropTargetEvent event) {
		fTarget = (Item) event.item;
		return fTarget;
	}
	
    /**
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
    	return ((BreakpointsViewer)getViewer()).canDrop(fTarget, (IStructuredSelection) LocalSelectionTransfer.getInstance().getSelection());
    }
}
