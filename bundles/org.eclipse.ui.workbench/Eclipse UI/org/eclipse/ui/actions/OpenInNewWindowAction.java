/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Opens a new window. The initial perspective
 * for the new window will be the same type as
 * the active perspective in the window which this
 * action is running in. The default input for the 
 * new window's page is application-specific.
 */
public class OpenInNewWindowAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    private IAdaptable pageInput;

    /**
     * Creates a new <code>OpenInNewWindowAction</code>. Sets
     * the new window page's input to be an application-specific
     * default.
     * 
     * @param window the workbench window containing this action
     */
    public OpenInNewWindowAction(IWorkbenchWindow window) {
        this(window, ((Workbench) window.getWorkbench()).getDefaultPageInput());
    }

    /**
     * Creates a new <code>OpenInNewWindowAction</code>.
     * 
     * @param window the workbench window containing this action
     * @param input the input for the new window's page
     */
    public OpenInNewWindowAction(IWorkbenchWindow window, IAdaptable input) {
        super(WorkbenchMessages.getString("OpenInNewWindowAction.text")); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        // @issue missing action id
        setToolTipText(WorkbenchMessages
                .getString("OpenInNewWindowAction.toolTip")); //$NON-NLS-1$
        pageInput = input;
        WorkbenchHelp.setHelp(this, IHelpContextIds.OPEN_NEW_WINDOW_ACTION);
    }

    /**
     * Set the input to use for the new window's page.
     */
    public void setPageInput(IAdaptable input) {
        pageInput = input;
    }

    /**
     * The implementation of this <code>IAction</code> method
     * opens a new window. The initial perspective
     * for the new window will be the same type as
     * the active perspective in the window which this
     * action is running in.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        try {
            String perspId;

            IWorkbenchPage page = workbenchWindow.getActivePage();
            if (page != null && page.getPerspective() != null)
                perspId = page.getPerspective().getId();
            else
                perspId = workbenchWindow.getWorkbench()
                        .getPerspectiveRegistry().getDefaultPerspective();

            workbenchWindow.getWorkbench().openWorkbenchWindow(perspId,
                    pageInput);
        } catch (WorkbenchException e) {
            ErrorDialog.openError(workbenchWindow.getShell(), WorkbenchMessages
                    .getString("OpenInNewWindowAction.errorTitle"), //$NON-NLS-1$,
                    e.getMessage(), e.getStatus());
        }
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     * @since 3.0
     */
    public void dispose() {
        workbenchWindow = null;
    }
}