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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.dialogs.WorkbenchEditorsDialog;

/**
 * Implements an action to open a dialog showing all open editors
 * and the recent closed editors.
 */
public class WorkbenchEditorsAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Constructor for NavigateWorkbenchAction.
     * 
     * @param window the window
     */
    public WorkbenchEditorsAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.WorkbenchEditorsAction_label);
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        // @issue missing action id
        workbenchWindow.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.WORKBENCH_EDITORS_ACTION);
        setActionDefinitionId("org.eclipse.ui.window.switchToEditor"); //$NON-NLS-1$
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
        if (page != null) {
            new WorkbenchEditorsDialog(workbenchWindow).open();
        }
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        workbenchWindow = null;
    }
}
