/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.ide.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.dialogs.CleanDialog;

/**
 * The clean action replaces the rebuild actions. Clean will discard all built
 * state for all projects in the workspace, and deletes all problem markers.
 * The next time a build is run, projects will have to be built from scratch.
 * Technically this is only necessary if an incremental builder misbehaves.
 * 
 * @since 3.0
 */
public class BuildCleanAction extends Action implements
        ActionFactory.IWorkbenchAction {
    private IWorkbenchWindow window;

    public BuildCleanAction(IWorkbenchWindow window) {
        super(IDEWorkbenchMessages.getString("Workbench.buildClean")); //$NON-NLS-1$
        setActionDefinitionId("org.eclipse.ui.project.cleanAction"); //$NON-NLS-1$
        this.window = window;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionFactory.IWorkbenchAction#dispose()
     */
    public void dispose() {
        //nothing to dispose
    }

    public void run() {
        ISelection selection = window.getSelectionService().getSelection();
        IProject[] selected = null;
        if (selection != null && !selection.isEmpty()
                && selection instanceof IStructuredSelection) {
            selected = BuildSetAction
                    .extractProjects(((IStructuredSelection) selection)
                            .toArray());
        } else {
            //see if we can extract a selected project from the active editor
            IWorkbenchPart part = window.getPartService().getActivePart();
            if (part instanceof IEditorPart) {
                IEditorInput input = ((IEditorPart) part).getEditorInput();
                if (input instanceof IFileEditorInput)
                    selected = new IProject[] { ((IFileEditorInput) input)
                            .getFile().getProject() };
            }
        }
        if (selected == null)
            selected = new IProject[0];
        new CleanDialog(window, selected).open();
    }
}