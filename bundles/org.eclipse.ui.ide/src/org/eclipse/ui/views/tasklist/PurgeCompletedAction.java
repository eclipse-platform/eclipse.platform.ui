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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This action deletes all the tasks found in the registry that
 * are marked as completed.
 */
class PurgeCompletedAction extends TaskAction {

    /**
     * Creates the action.
     */
    public PurgeCompletedAction(TaskList tasklist, String id) {
        super(tasklist, id);
        WorkbenchHelp.setHelp(this,
                ITaskListHelpContextIds.PURGE_COMPLETED_TASK_ACTION);
    }

    /**
     * Fetches all the completed tasks in the workspace and deletes them.
     */
    public void run() {
        try {
            IResource resource = getTaskList().getResource();
            int depth = getTaskList().getResourceDepth();
            IMarker[] tasks = resource.findMarkers(IMarker.TASK, true, depth);
            final List completed = new ArrayList();
            for (int i = 0; i < tasks.length; i++) {
                IMarker task = tasks[i];
                if (MarkerUtil.isComplete(task) && !MarkerUtil.isReadOnly(task)) {
                    completed.add(task);
                }
            }
            // Check if there is anything to do
            if (completed.size() == 0) {
                MessageDialog.openInformation(getShell(), TaskListMessages
                        .getString("PurgeCompleted.title"), //$NON-NLS-1$
                        TaskListMessages
                                .getString("PurgeCompleted.noneCompleted")); //$NON-NLS-1$
                return;
            }

            // Verify.
            if (!MessageDialog.openConfirm(getShell(), TaskListMessages
                    .getString("PurgeCompleted.title"), //$NON-NLS-1$
                    TaskListMessages.format("PurgeCompleted.permanent", //$NON-NLS-1$
                            new Object[] { new Integer(completed.size()) }))) {
                return;
            }

            IMarker[] toDelete = new IMarker[completed.size()];
            completed.toArray(toDelete);
            getTaskList().getWorkspace().deleteMarkers(toDelete);
        } catch (CoreException e) {
            ErrorDialog
                    .openError(
                            getShell(),
                            TaskListMessages
                                    .getString("PurgeCompleted.errorMessage"), null, e.getStatus()); //$NON-NLS-1$
        }
    }
}