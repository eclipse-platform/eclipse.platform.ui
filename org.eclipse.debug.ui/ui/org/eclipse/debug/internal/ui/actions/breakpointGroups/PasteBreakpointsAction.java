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
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.List;

import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.debug.internal.ui.views.breakpoints.OtherBreakpointCategory;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * Standard action for pasting resources on the clipboard to the selected resource's location.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class PasteBreakpointsAction extends BreakpointSelectionAction {

    /**
     * System clipboard
     */
    private Clipboard clipboard;
    
    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     */
    public PasteBreakpointsAction(BreakpointsView view, Clipboard clipboard) {
        super(BreakpointGroupMessages.getString("PasteBreakpointsAction.0"), view); //$NON-NLS-1$
        Assert.isNotNull(clipboard);
        this.clipboard = clipboard;
        setToolTipText(BreakpointGroupMessages.getString("PasteBreakpointsAction.1")); //$NON-NLS-1$
    }

    /**
     * Returns the actual target of the paste action. Returns null
     * if no valid target is selected.
     * 
     * @return the actual target of the paste action
     */
    private Object getTarget() {
        List selectedNonResources = getSelectedNonResources();
        if (selectedNonResources.size() == 1) {
            Object target = selectedNonResources.get(0);
            if (target instanceof BreakpointContainer) {
                return target;
            }
        }
        return null;
    }

    /**
     * Implementation of method defined on <code>IAction</code>.
     */
    public void run() {
        getBreakpointsView().performPaste(getTarget(), LocalSelectionTransfer.getInstance().getSelection());
    }

    /**
     * Returns whether this action should be enabled based on the selection
     * in the clipboard. Only updates when the breakpoints view has focus. 
     */
    protected boolean updateSelection(IStructuredSelection selection) {
    	// only update when view has focus
    	if (!getBreakpointsView().getViewer().getControl().isFocusControl()) {
    		return false;
    	}
    	
        // can't paste into "Others" (only move)
        Object target = getTarget();
        if (target instanceof BreakpointContainer) {
            BreakpointContainer container = (BreakpointContainer) target;
            if (container.getCategory() instanceof OtherBreakpointCategory) {
                return false;
            }
        }

        final ISelection[] clipboardData = new ISelection[1];
        getBreakpointsView().getSite().getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                // clipboard must have resources or files
                LocalSelectionTransfer transfer = LocalSelectionTransfer.getInstance();
                clipboardData[0] = (ISelection) clipboard.getContents(transfer);
            }
        });
        ISelection pasteSelection = clipboardData[0];
        
        return getBreakpointsView().canPaste(target, pasteSelection);
    }
}

