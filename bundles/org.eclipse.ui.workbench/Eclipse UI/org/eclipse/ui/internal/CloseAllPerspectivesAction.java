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
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The <code>CloseAllPerspectivesAction</code> is used to close all of 
 * the opened perspectives in the workbench window's active page.
 */
public class CloseAllPerspectivesAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Create a new instance of <code>CloseAllPerspectivesAction</code>
     * 
     * @param window the workbench window this action applies to
     */
    public CloseAllPerspectivesAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("CloseAllPerspectivesAction.text")); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setActionDefinitionId("org.eclipse.ui.window.closeAllPerspectives"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages
                .getString("CloseAllPerspectivesAction.toolTip")); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this, IWorkbenchHelpContextIds.CLOSE_ALL_PAGES_ACTION);
        setEnabled(false);
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
            page.closeAllPerspectives(true, true);
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