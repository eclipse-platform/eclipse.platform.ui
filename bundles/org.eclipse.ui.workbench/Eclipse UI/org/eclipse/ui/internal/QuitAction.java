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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Try to quit the application.
 */
public class QuitAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Creates a new <code>QuitAction</code>.
     */
    public QuitAction(IWorkbenchWindow window) {
        // Although window is not currently used,
        // this follows the same pattern as other ActionFactory actions.
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setText(WorkbenchMessages.getString("Exit.text")); //$NON-NLS-1$
        setToolTipText(WorkbenchMessages.getString("Exit.toolTip")); //$NON-NLS-1$
        setActionDefinitionId("org.eclipse.ui.file.exit"); //$NON-NLS-1$
        WorkbenchHelp.setHelp(this, IHelpContextIds.QUIT_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        PlatformUI.getWorkbench().close();
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        workbenchWindow = null;
    }

}