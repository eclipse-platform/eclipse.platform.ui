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
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Reset the layout within the active perspective.
 */
public class ResetPerspectiveAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * This default constructor allows the the action to be called from the welcome page.
     */
    public ResetPerspectiveAction() {
        this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

    /**
     * Create an instance of this class
     * @param window the window
     */
    public ResetPerspectiveAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.ResetPerspective_text); 
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setActionDefinitionId("org.eclipse.ui.window.resetPerspective"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages.ResetPerspective_toolTip); 
        setEnabled(false);
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.RESET_PERSPECTIVE_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        IWorkbenchPage page = workbenchWindow.getActivePage();
        if (page != null && page.getPerspective() != null) {
            String message = NLS.bind(WorkbenchMessages.ResetPerspective_message, page.getPerspective().getLabel() );
            String[] buttons = new String[] { IDialogConstants.OK_LABEL,
                    IDialogConstants.CANCEL_LABEL };
            MessageDialog d = new MessageDialog(workbenchWindow.getShell(),
                    WorkbenchMessages.ResetPerspective_title,
                    null, message, MessageDialog.QUESTION, buttons, 0);
            if (d.open() == 0)
                page.resetPerspective();
        }
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // already disposed
            return;
        }
        workbenchWindow = null;
    }

}
