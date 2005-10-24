/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * @since 3.0
 */
public class MinimizePartAction extends PageEventAction {

    /**
     * Creates a MaximizePartAction.
     * 
     * @param window the window
     */
    public MinimizePartAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.MinimizePartAction_text, window); 
        setToolTipText(WorkbenchMessages.MinimizePartAction_toolTip);
        // @issue missing action id
        updateState();
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.MINIMIZE_PART_ACTION);
        setActionDefinitionId("org.eclipse.ui.window.minimizePart"); //$NON-NLS-1$
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
                    ((WorkbenchPage) page).setState(partRef, IStackPresentationSite.STATE_MINIMIZED);
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
