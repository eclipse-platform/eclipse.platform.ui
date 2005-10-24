/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Toggles the maximize/restore state of the active part, if there is one.
 */
public class MaximizePartAction extends PageEventAction {

    /**
     * Creates a MaximizePartAction.
     * 
     * @param window the window
     */
    public MaximizePartAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.MaximizePartAction_text, window);
        setToolTipText(WorkbenchMessages.MaximizePartAction_toolTip);
        // @issue missing action id
        updateState();
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.MAXIMIZE_PART_ACTION);
        setActionDefinitionId("org.eclipse.ui.window.maximizePart"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void pageActivated(IWorkbenchPage page) {
        super.pageActivated(page);
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on PageEventAction.
     */
    public void pageClosed(IWorkbenchPage page) {
        super.pageClosed(page);
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (getWorkbenchWindow() == null) {
            // action has been dispose
            return;
        }

        IWorkbenchPage page = getActivePage();
        if (page != null) {
            if (page instanceof WorkbenchPage) {
                IWorkbenchPartReference partRef = page.getActivePartReference();

                if (partRef != null) {
                    ((WorkbenchPage) page).toggleZoom(partRef);
                }
            }
        }
    }

    /**
     * Updates the enabled state.
     */
    private void updateState() {
        setEnabled(getActivePage() != null);
    }
}
