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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.DeleteMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.views.tasklist.TaskListMessages;

/**
 * This action deletes all the tasks found in the registry that
 * are marked as completed.
 */
class PurgeCompletedAction extends TaskAction {

    /**
     * Creates the action.
     * 
     * @param tasklist the task list
     * @param id the id
     */
    public PurgeCompletedAction(TaskList tasklist, String id) {
        super(tasklist, id);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                ITaskListHelpContextIds.PURGE_COMPLETED_TASK_ACTION);
    }

    /**
     * Fetches all the completed tasks in the workspace and deletes them.
     */
    public void run() {
        IResource resource = getTaskList().getResource();
        int depth = getTaskList().getResourceDepth();
        IMarker[] tasks;
        try {
        	tasks = resource.findMarkers(IMarker.TASK, true, depth);
        } catch (CoreException e) {
            ErrorDialog.openError(
                    getShell(),
                    TaskListMessages.PurgeCompleted_errorMessage, null, e.getStatus()); 

        	return;
        }
        final List completed = new ArrayList();
        for (int i = 0; i < tasks.length; i++) {
            IMarker task = tasks[i];
            if (MarkerUtil.isComplete(task) && !MarkerUtil.isReadOnly(task)) {
                completed.add(task);
            }
        }
        // Check if there is anything to do
        if (completed.size() == 0) {
            MessageDialog.openInformation(getShell(), TaskListMessages.PurgeCompleted_title, 
                    TaskListMessages.PurgeCompleted_noneCompleted); 
            return;
        }

        // Verify.
        if (!MessageDialog.openConfirm(getShell(), TaskListMessages.PurgeCompleted_title,
                NLS.bind(TaskListMessages.PurgeCompleted_permanent,String.valueOf(completed.size())))) {
            return;
        }

        IMarker[] toDelete = new IMarker[completed.size()];
        completed.toArray(toDelete);
		IUndoableOperation op = new DeleteMarkersOperation(toDelete, getText());
		execute(op, TaskListMessages.PurgeCompleted_errorMessage, null,
				WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
    }
}
