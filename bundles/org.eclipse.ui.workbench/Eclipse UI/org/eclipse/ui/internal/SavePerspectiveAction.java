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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.SavePerspectiveDialog;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;

/**
 * Action to save the layout of the active perspective.
 */
public class SavePerspectiveAction extends Action implements
        ActionFactory.IWorkbenchAction {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Creates an instance of this class.
     *
     * @param window the workbench window in which this action appears
     */
    public SavePerspectiveAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("SavePerspective.text")); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        setActionDefinitionId("org.eclipse.ui.window.savePerspective"); //$NON-NLS-1$
        // @issue missing action id
        setToolTipText(WorkbenchMessages.getString("SavePerspective.toolTip")); //$NON-NLS-1$
        setEnabled(false);
        WorkbenchHelp.setHelp(this, IWorkbenchHelpContextIds.SAVE_PERSPECTIVE_ACTION);
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
        if (page == null) {
            return;
        }
        PerspectiveDescriptor desc = (PerspectiveDescriptor) page
                .getPerspective();
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
        MessageDialog d = new MessageDialog(workbenchWindow.getShell(),
                WorkbenchMessages.getString("SavePerspective.overwriteTitle"), //$NON-NLS-1$
                null, WorkbenchMessages
                        .getString("SavePerspective.singletonQuestion"), //$NON-NLS-1$
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
        SavePerspectiveDialog dlg = new SavePerspectiveDialog(workbenchWindow
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
                MessageDialog.openError(dlg.getShell(), WorkbenchMessages
                        .getString("SavePerspective.errorTitle"), //$NON-NLS-1$
                        WorkbenchMessages
                                .getString("SavePerspective.errorMessage")); //$NON-NLS-1$
                return;
            }
        }

        // Save state.
        page.savePerspectiveAs(newDesc);
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