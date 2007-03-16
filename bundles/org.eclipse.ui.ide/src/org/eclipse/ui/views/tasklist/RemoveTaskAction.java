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

import java.util.List;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.DeleteMarkersOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.views.tasklist.TaskListMessages;

/**
 * This action removes the selected task(s) from the task list.
 * Only tasks can be removed. Problems can only disappear from
 * the task list when they are fixed in the associated code.
 */
class RemoveTaskAction extends TaskAction {

    /**
     * Creates the action.
     * 
     * @param tasklist the task list
     * @param id the id
     */
    public RemoveTaskAction(TaskList tasklist, String id) {
        super(tasklist, id);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				ITaskListHelpContextIds.REMOVE_TASK_ACTION);
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
        List list = selection.toList();
        IMarker[] markers = new IMarker[list.size()];
        list.toArray(markers);
     	IUndoableOperation op = new DeleteMarkersOperation(markers, TaskListMessages.RemoveTask_undoText);
   		execute(op, TaskListMessages.RemoveTask_errorMessage, null,
   				WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
        // set the selection to be the one which took the place of the one with focus
        int count = table.getItemCount();
        if (focusIndex < count) {
        	table.setSelection(focusIndex);
        } else if (count != 0) {
        	table.setSelection(count - 1);
        }
        // update the viewer's selection, since setting the table selection does not notify the viewer
        viewer.setSelection(viewer.getSelection(), true);
    }
}
