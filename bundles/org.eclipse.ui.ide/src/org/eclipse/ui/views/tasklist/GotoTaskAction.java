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

package org.eclipse.ui.views.tasklist;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;

/**
 * This action opens an editor for the resource
 * associated with the selected marker, and
 * jumps to the marker's location in the editor.
 */
class GotoTaskAction extends TaskAction {

    /**
     * Creates the action.
     */
    public GotoTaskAction(TaskList tasklist, String id) {
        super(tasklist, id);
        WorkbenchHelp.setHelp(this, ITaskListHelpContextIds.GOTO_TASK_ACTION);
    }

    /**
     * Performs this action. This action works only for single selection.
     */
    public void run() {
        IStructuredSelection selection = (IStructuredSelection) getTaskList()
                .getSelection();
        Object o = selection.getFirstElement();
        if (!(o instanceof IMarker))
            return;
        IMarker marker = (IMarker) o;
        IResource resource = marker.getResource();
        if (marker.exists() && resource instanceof IFile) {
            IWorkbenchPage page = getTaskList().getSite().getPage();
            try {
                IDE.openEditor(page, marker, OpenStrategy.activateOnOpen());
            } catch (PartInitException e) {
                DialogUtil.openError(page.getWorkbenchWindow().getShell(),
                        TaskListMessages.getString("GotoTask.errorMessage"), //$NON-NLS-1$
                        e.getMessage(), e);
            }
        }
    }
}