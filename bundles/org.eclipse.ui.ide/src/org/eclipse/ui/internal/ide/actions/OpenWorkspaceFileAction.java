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

package org.eclipse.ui.internal.ide.actions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.OpenResourceDialog;

/**
 * Implements the open resource action. 
 * Opens a dialog prompting for a file and opens the selected file in an editor.
 * 
 * @since 2.1
 */
public class OpenWorkspaceFileAction extends Action implements
        IWorkbenchWindowActionDelegate {
    IWorkbenchWindow workbenchWindow;

    /**
     * Creates a new instance of the class.
     */
    public OpenWorkspaceFileAction() {
        super();
        WorkbenchHelp.setHelp(this, IHelpContextIds.OPEN_WORKSPACE_FILE_ACTION);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    public void dispose() {
        // do nothing
    }

    /**
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    public void init(IWorkbenchWindow window) {
        workbenchWindow = window;
    }

    /**
     * Query the user for the resource that should be opened
     * 
     * @return the resource that should be opened or null if the 
     * 	resource selection dialog was cancelled.
     */
    IFile queryFileResource() {
        Shell parent = workbenchWindow.getShell();
        IContainer input = ResourcesPlugin.getWorkspace().getRoot();

        OpenResourceDialog dialog = new OpenResourceDialog(parent, input,
                IResource.FILE);
        int resultCode = dialog.open();
        if (resultCode != IDialogConstants.OK_ID)
            return null;

        Object[] result = dialog.getResult();
        if (result == null || result.length == 0
                || result[0] instanceof IFile == false)
            return null;

        return (IFile) result[0];
    }

    /**
     * Collect all resources in the workbench, open a dialog asking the user to
     * select a file and open the file in an editor.
     */
    public void run(IAction action) {
        IFile file = queryFileResource();

        if (file == null)
            return;
        try {
            IWorkbenchPage page = workbenchWindow.getActivePage();
            if (page != null) {
                IDE.openEditor(page, file, true);
            }
        } catch (CoreException x) {
            String title = IDEWorkbenchMessages
                    .getString("OpenWorkspaceFileAction.errorTitle"); //$NON-NLS-1$
            String message = IDEWorkbenchMessages
                    .getString("OpenWorkspaceFileAction.errorMessage"); //$NON-NLS-1$
            IDEWorkbenchPlugin.log(title, x.getStatus());
            ErrorDialog.openError(workbenchWindow.getShell(), title, message, x
                    .getStatus());
        }
    }

    /**
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing
    }

}