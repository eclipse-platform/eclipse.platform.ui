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

import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Edit the action sets.
 */
public class EditActionSetsAction extends PerspectiveAction {

    /**
     * This default constructor allows the the action to be called from the welcome page.
     */
    public EditActionSetsAction() {
        this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

    /**
     * Create a new instance of this class.
     * 
     * @param window the window
     */
    public EditActionSetsAction(IWorkbenchWindow window) {
        super(window);
        setText(WorkbenchMessages.EditActionSetsAction_text); 
        setActionDefinitionId("org.eclipse.ui.window.customizePerspective"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages.EditActionSetsAction_toolTip); 
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.EDIT_ACTION_SETS_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    protected void run(IWorkbenchPage page, IPerspectiveDescriptor persp) {
        ((WorkbenchPage) page).editActionSets();
    }

}
