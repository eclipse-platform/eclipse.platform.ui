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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.dialogs.SavePerspectiveDialog;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;

/**
 * Action to save the layout of the active perspective.
 */
public class SavePerspectiveAction extends PerspectiveAction {

    /**
     * Creates an instance of this class.
     *
     * @param window the workbench window in which this action appears
     */
    public SavePerspectiveAction(IWorkbenchWindow window) {
        super(window);
        setText(WorkbenchMessages.SavePerspective_text);
        setActionDefinitionId("org.eclipse.ui.window.savePerspective"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages.SavePerspective_toolTip); 
        window.getWorkbench().getHelpSystem().setHelp(this,
				IWorkbenchHelpContextIds.SAVE_PERSPECTIVE_ACTION);
    }

    /* (non-Javadoc)
     * Method declared on PerspectiveAction.
     */
    protected void run(IWorkbenchPage page, IPerspectiveDescriptor persp) {
        PerspectiveDescriptor desc = (PerspectiveDescriptor) persp;
        if (desc != null) {
            if (desc.isSingleton()) {
                saveSingleton(page);
            } else {
                saveNonSingleton(page, desc);
            }
        }
    }

    /** 
     * Save a singleton over itself.
     */
    private void saveSingleton(IWorkbenchPage page) {
        String[] buttons = new String[] { IDialogConstants.OK_LABEL,
                IDialogConstants.CANCEL_LABEL };
        MessageDialog d = new MessageDialog(page.getWorkbenchWindow().getShell(),
                WorkbenchMessages.SavePerspective_overwriteTitle,
                null, WorkbenchMessages.SavePerspective_singletonQuestion,
                MessageDialog.QUESTION, buttons, 0);
        if (d.open() == 0) {
            page.savePerspective();
        }
    }

    /**
     * Save a singleton over the user selection.
     */
    private void saveNonSingleton(IWorkbenchPage page, PerspectiveDescriptor oldDesc) {
        // Get reg.
        PerspectiveRegistry reg = (PerspectiveRegistry) WorkbenchPlugin
                .getDefault().getPerspectiveRegistry();

        // Get persp name.
        SavePerspectiveDialog dlg = new SavePerspectiveDialog(page.getWorkbenchWindow()
                .getShell(), reg);
        // Look up the descriptor by id again to ensure it is still valid.
        IPerspectiveDescriptor description = reg.findPerspectiveWithId(oldDesc.getId());
        dlg.setInitialSelection(description);
        if (dlg.open() != IDialogConstants.OK_ID) {
            return;
        }

        // Create descriptor.
        PerspectiveDescriptor newDesc = (PerspectiveDescriptor) dlg.getPersp();
        if (newDesc == null) {
            String name = dlg.getPerspName();
            newDesc = reg.createPerspective(name,
                    (PerspectiveDescriptor) description);
            if (newDesc == null) {
                MessageDialog.openError(dlg.getShell(), WorkbenchMessages.SavePerspective_errorTitle,
                        WorkbenchMessages.SavePerspective_errorMessage); 
                return;
            }
        }

        // Save state.
        page.savePerspectiveAs(newDesc);
    }

}
