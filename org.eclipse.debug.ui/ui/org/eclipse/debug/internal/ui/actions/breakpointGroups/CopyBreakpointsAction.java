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
package org.eclipse.debug.internal.ui.actions.breakpointGroups;

import java.util.Iterator;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * Action for copying the currently selected breakpoints to the clipboard.
 */
public class CopyBreakpointsAction extends BreakpointSelectionAction {

    /**
     * System clipboard
     */
    private Clipboard clipboard;

    /**
     * Associated paste action. May be <code>null</code>
     */
    private PasteBreakpointsAction pasteAction;
    
    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     * @param clipboard a platform clipboard
     */
    public CopyBreakpointsAction(BreakpointsView view, Clipboard clipboard) {
        super(BreakpointGroupMessages.CopyBreakpointsAction_0, view); 
        Assert.isNotNull(clipboard);
        this.clipboard = clipboard;
        setToolTipText(BreakpointGroupMessages.CopyBreakpointsAction_1); 
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
        IStructuredSelection selection = getStructuredSelection();
        Object[] objects = selection.toArray();
        StringBuffer buffer = new StringBuffer();
        ILabelProvider labelProvider = (ILabelProvider) ((StructuredViewer)getBreakpointsView().getViewer()).getLabelProvider();
        for (int i = 0; i < objects.length; i++) {
            Object object = objects[i];
            if (i > 0) {
                buffer.append("\n"); //$NON-NLS-1$
            }
            buffer.append(labelProvider.getText(object));
        }
        setClipboard(selection, buffer.toString());

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
    private void setClipboard(ISelection selection, String text) {
        try {
            LocalSelectionTransfer.getInstance().setSelection(selection);
            LocalSelectionTransfer.getInstance().setSelectionSetTime(System.currentTimeMillis());
            clipboard.setContents(new Object[] {selection, text}, new Transfer[] {LocalSelectionTransfer.getInstance(), TextTransfer.getInstance()});
        } catch (SWTError e) {
            if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
                throw e;
            if (MessageDialog.openQuestion(
                    getBreakpointsView().getSite().getShell(), BreakpointGroupMessages.CopyBreakpointsAction_2, 
                    BreakpointGroupMessages.CopyBreakpointsAction_3)) { 
                setClipboard(selection, text);
            }
        }
    }

    /**
     * Enables if one or more breakpoints are selected.
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }
        Iterator iterator = selection.iterator();
        while (iterator.hasNext()) {
            if (!(iterator.next() instanceof IBreakpoint)) {
                return false;
            }
        }
        return true;
    }

}

