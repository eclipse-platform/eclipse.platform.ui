/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.views.tasklist.TaskListMessages;

/**
 * This action opens an editor for the resource
 * associated with the selected marker, and
 * jumps to the marker's location in the editor.
 */
class GotoTaskAction extends TaskAction {

    /**
     * Creates the action.
     * 
     * @param tasklist the task list
     * @param id the id
     */
    public GotoTaskAction(TaskList tasklist, String id) {
        super(tasklist, id);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				ITaskListHelpContextIds.GOTO_TASK_ACTION);
    }

    /**
     * Performs this action. This action works only for single selection.
     */
    public void run() {
        IStructuredSelection selection = (IStructuredSelection) getTaskList()
                .getSelection();
        Object o = selection.getFirstElement();
        if (!(o instanceof IMarker)) {
			return;
		}
        IMarker marker = (IMarker) o;
        IResource resource = marker.getResource();
        if (marker.exists() && resource instanceof IFile) {
            IWorkbenchPage page = getTaskList().getSite().getPage();
            try {
                IDE.openEditor(page, marker, OpenStrategy.activateOnOpen());
            } catch (PartInitException e) {
                DialogUtil.openError(page.getWorkbenchWindow().getShell(),
                        TaskListMessages.GotoTask_errorMessage, 
                        e.getMessage(), e);
            }
        }
    }
}
