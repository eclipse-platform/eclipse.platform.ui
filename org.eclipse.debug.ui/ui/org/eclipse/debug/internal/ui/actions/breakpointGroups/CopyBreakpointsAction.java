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

import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * Action for copying the currently selected breakpoints to the clipboard.
 */
public class CopyBreakpointsAction extends SelectionListenerAction {

    /**
     * System clipboard
     */
    private Clipboard clipboard;

    /**
     * Associated paste action. May be <code>null</code>
     */
    private PasteBreakpointsAction pasteAction;
    
    /**
     * Associated view.
     */
    private BreakpointsView breakpointsView;

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * @param clipboard a platform clipboard
     */
    public CopyBreakpointsAction(BreakpointsView view, Clipboard clipboard) {
        super(BreakpointGroupMessages.getString("CopyBreakpointsAction.0")); //$NON-NLS-1$
        Assert.isNotNull(clipboard);
        this.clipboard = clipboard;
        breakpointsView = view;
        setToolTipText(BreakpointGroupMessages.getString("CopyBreakpointsAction.1")); //$NON-NLS-1$
    }

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * @param clipboard a platform clipboard
     * @param pasteAction a paste action
     */
    public CopyBreakpointsAction(BreakpointsView view, Clipboard clipboard, PasteBreakpointsAction pasteAction) {
        this(view, clipboard);
        this.pasteAction = pasteAction;
    }

    /**
     * The <code>CopyAction</code> implementation of this method defined 
     * on <code>IAction</code> copies the selected resources to the 
     * clipboard.
     */
    public void run() {
        setClipboard(getStructuredSelection());

        // update the enablement of the paste action
        // workaround since the clipboard does not suppot callbacks
        if (pasteAction != null && pasteAction.getStructuredSelection() != null)
            pasteAction.selectionChanged(pasteAction.getStructuredSelection());
    }

    /**
     * Set the clipboard contents. Prompt to retry if clipboard is busy.
     * 
     * @param selection the selection to copy to the clipboard
     */
    private void setClipboard(ISelection selection) {
        try {
            LocalSelectionTransfer.getInstance().setSelection(selection);
            LocalSelectionTransfer.getInstance().setSelectionSetTime(System.currentTimeMillis());
            clipboard.setContents(new Object[] {selection}, new Transfer[] {LocalSelectionTransfer.getInstance()});
        } catch (SWTError e) {
            if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
                throw e;
            if (MessageDialog.openQuestion(
                    breakpointsView.getSite().getShell(), BreakpointGroupMessages.getString("CopyBreakpointsAction.2"), //$NON-NLS-1$
                    BreakpointGroupMessages.getString("CopyBreakpointsAction.3"))) { //$NON-NLS-1$
                setClipboard(selection);
            }
        }
    }

    /**
     * The <code>CopyAction</code> implementation of this
     * <code>SelectionListenerAction</code> method enables this action if 
     * one or more resources of compatible types are selected.
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        return breakpointsView.canMove(selection);
    }

}

