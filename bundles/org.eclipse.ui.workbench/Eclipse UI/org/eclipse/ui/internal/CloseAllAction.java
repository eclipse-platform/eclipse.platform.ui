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

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 *	Closes all active editors
 */
public class CloseAllAction extends PageEventAction {

    /**
     *	Create an instance of this class
     */
    public CloseAllAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("CloseAllAction.text"), window); //$NON-NLS-1$
        setToolTipText(WorkbenchMessages.getString("CloseAllAction.toolTip")); //$NON-NLS-1$
        setEnabled(false);
        setId("closeAll"); //$NON-NLS-1$
        updateState();
        WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_ALL_ACTION);
        setActionDefinitionId("org.eclipse.ui.file.closeAll"); //$NON-NLS-1$
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
     * Method declared on PartEventAction.
     */
    public void partClosed(IWorkbenchPart part) {
        super.partClosed(part);
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on PartEventAction.
     */
    public void partOpened(IWorkbenchPart part) {
        super.partOpened(part);
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on Action.
     */
    public void run() {
        if (getWorkbenchWindow() == null) {
            // action has been disposed
            return;
        }
        IWorkbenchPage page = getActivePage();
        if (page != null) {
            page.closeAllEditors(true);
        }
    }

    /**
     * Enable the action if there at least one editor open.
     */
    private void updateState() {
        IWorkbenchPage page = getActivePage();
        if (page != null) {
            setEnabled(page.getEditorReferences().length >= 1);
        } else {
            setEnabled(false);
        }
    }
}