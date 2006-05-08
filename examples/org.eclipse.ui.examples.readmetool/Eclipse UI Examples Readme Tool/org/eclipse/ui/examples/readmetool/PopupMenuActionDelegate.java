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
package org.eclipse.ui.examples.readmetool;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Action delegate for handling popup menu actions on a readme file.
 */
public class PopupMenuActionDelegate implements IObjectActionDelegate {

    private IWorkbenchPart part;

    /** (non-Javadoc)
     * Method declared on IDropActionDelegate
     */
    public void run(IAction action) {
        MessageDialog.openInformation(this.part.getSite().getShell(),
                MessageUtil.getString("Readme_Example"), //$NON-NLS-1$
                MessageUtil.getString("Popup_Menu_Action_executed")); //$NON-NLS-1$
    }

    /** (non-Javadoc)
     * Method declared on IActionDelegate
     */
    public void selectionChanged(IAction action, ISelection selection) {
        //Ignored for this example
    }

    /** (non-Javadoc)
     * Method declared on IObjectActionDelegate
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.part = targetPart;
    }
}
