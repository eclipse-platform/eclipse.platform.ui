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

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This action removes the selected task(s) from the task list.
 * Only tasks can be removed. Problems can only disappear from
 * the task list when they are fixed in the associated code.
 */
class RemoveTaskAction extends TaskAction {

    /**
     * Creates the action.
     */
    public RemoveTaskAction(TaskList tasklist, String id) {
        super(tasklist, id);
        WorkbenchHelp.setHelp(this, ITaskListHelpContextIds.REMOVE_TASK_ACTION);
    }

    /**
     * Removes all the tasks in the current selection from the task list.
     */
    public void run() {
        TaskList taskList = getTaskList();
        TableViewer viewer = taskList.getTableViewer();
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        if (selection.isEmpty()) {
            return;
        }
        taskList.cancelEditing();
        // get the index of the selected item which has focus
        Table table = viewer.getTable();
        int focusIndex = table.getSelectionIndex();
        try {
            List list = ((IStructuredSelection) selection).toList();
            IMarker[] markers = new IMarker[list.size()];
            list.toArray(markers);
            // be sure to only invoke one workspace operation
            taskList.getWorkspace().deleteMarkers(markers);
            // set the selection to be the one which took the place of the one with focus
            int count = table.getItemCount();
            if (focusIndex < count) {
                table.setSelection(focusIndex);
            } else if (count != 0) {
                table.setSelection(count - 1);
            }
            // update the viewer's selection, since setting the table selection does not notify the viewer
            viewer.setSelection(viewer.getSelection(), true);

        } catch (CoreException e) {
            ErrorDialog.openError(getShell(), TaskListMessages
                    .getString("RemoveTask.errorMessage"), //$NON-NLS-1$
                    null, e.getStatus());
        }
    }
}