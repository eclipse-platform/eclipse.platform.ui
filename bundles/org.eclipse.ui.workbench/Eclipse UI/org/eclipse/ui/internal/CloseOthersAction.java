/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 *	Closes all editors except the one that is active.
 */
public class CloseOthersAction extends PageEventAction {

    /**
     * Create an instance of this class.
     *
     * @param window the window
     */
    public CloseOthersAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.CloseOthersAction_text, window);
        setToolTipText(WorkbenchMessages.CloseOthersAction_toolTip);
        setEnabled(false);
        setId("closeOthers"); //$NON-NLS-1$
        updateState();
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.CLOSE_OTHERS_ACTION);
        setActionDefinitionId("org.eclipse.ui.file.closeOthers"); //$NON-NLS-1$
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
        	IEditorReference[] refArray = page.getEditorReferences();
        	if (refArray != null && refArray.length > 1) {
               	IEditorReference[] otherEditors = new IEditorReference[refArray.length - 1];
            	IEditorReference activeEditor = (IEditorReference) page.getReference(page.getActiveEditor());
            	for(int i = 0; i < refArray.length; i++) {
            		if(refArray[i] != activeEditor)
            			continue;
            		System.arraycopy(refArray, 0, otherEditors, 0, i);
            		System.arraycopy(refArray, i+1, otherEditors, i, refArray.length - 1 - i);
            		break;
            	}
                page.closeEditors(otherEditors, true);
        	}
         }
    }

    /**
     * Enable the action if there is more than one editor open.
     */
    private void updateState() {
        IWorkbenchPage page = getActivePage();
        if (page != null) {
            setEnabled(page.getEditorReferences().length > 1);
        } else {
            setEnabled(false);
        }
    }
}
