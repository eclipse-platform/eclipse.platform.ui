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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointContainer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointSetOrganizer;
import org.eclipse.debug.internal.ui.views.breakpoints.BreakpointsView;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

/**
 * Standard action for pasting resources on the clipboard to the selected resource's location.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.0
 */
public class PasteBreakpointsAction extends SelectionListenerAction {

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID + ".PasteAction";//$NON-NLS-1$

    /**
     * System clipboard
     */
    private Clipboard clipboard;
    
    /**
     * The breakpoints view
     */
    private BreakpointsView breakpointsView;

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     */
    public PasteBreakpointsAction(BreakpointsView view, Clipboard clipboard) {
        super(BreakpointGroupMessages.getString("PasteBreakpointsAction.0")); //$NON-NLS-1$
        Assert.isNotNull(clipboard);
        this.clipboard = clipboard;
        breakpointsView = view;
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
        ISelection selection = LocalSelectionTransfer.getInstance().getSelection();
        Object target = getTarget();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object[] objects = ss.toArray();
            IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof IBreakpoint) {
                    IBreakpoint breakpoint = (IBreakpoint) objects[i];
                    try {
                        BreakpointSetOrganizer.setAddToDefault(false);
                        breakpointManager.addBreakpoint(breakpoint);
                    } catch (CoreException e) {
                        DebugUIPlugin.log(e);
                    } finally {
                        BreakpointSetOrganizer.setAddToDefault(true);
                    }
                }
            }
        }
        breakpointsView.performPaste(target, selection);
    }

    /**
     * The <code>PasteAction</code> implementation of this
     * <code>SelectionListenerAction</code> method enables this action if 
     * a resource compatible with what is on the clipboard is selected.
     * 
     * -Clipboard must have IResource or java.io.File
     * -Projects can always be pasted if they are open
     * -Workspace folder may not be copied into itself
     * -Files and folders may be pasted to a single selected folder in open 
     *  project or multiple selected files in the same folder 
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        if (!super.updateSelection(selection))
            return false;

        final ISelection[] clipboardData = new ISelection[1];
        breakpointsView.getSite().getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                // clipboard must have resources or files
                LocalSelectionTransfer transfer = LocalSelectionTransfer.getInstance();
                clipboardData[0] = (ISelection) clipboard.getContents(transfer);
            }
        });
        ISelection pasteSelection = clipboardData[0];
        Object target = getTarget();
        return breakpointsView.canPaste(target, pasteSelection);
    }
}

