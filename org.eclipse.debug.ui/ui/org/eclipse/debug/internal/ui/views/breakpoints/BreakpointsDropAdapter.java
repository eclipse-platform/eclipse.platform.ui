/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * BreakpointsDropAdapter
 */
public class BreakpointsDropAdapter extends ViewerDropAdapter {
    
    private BreakpointsView fView;

    /**
     * @param viewer
     */
    protected BreakpointsDropAdapter(BreakpointsView view, Viewer viewer) {
        super(viewer);
        fView = view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    public boolean performDrop(Object data) {
        return fView.performPaste(getCurrentTarget(), LocalSelectionTransfer.getInstance().getSelection());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        return fView.canPaste(target, LocalSelectionTransfer.getInstance().getSelection());
    }

}
