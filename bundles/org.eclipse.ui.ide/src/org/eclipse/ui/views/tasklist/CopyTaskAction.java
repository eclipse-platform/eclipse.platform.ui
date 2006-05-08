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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.tasklist.TaskListMessages;
import org.eclipse.ui.part.MarkerTransfer;

/**
 * Copies a task to the clipboard.
 */
class CopyTaskAction extends TaskAction {
 
	/**
     * Creates the action.
	 * @param tasklist the task list
	 * @param id the id
     */
    public CopyTaskAction(TaskList tasklist, String id) {
        super(tasklist, id);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				ITaskListHelpContextIds.COPY_TASK_ACTION);
    }

    /**
     * Performs this action.
     */
    public void run() {
        // Get the selected markers
        TaskList taskList = getTaskList();
        TableViewer viewer = taskList.getTableViewer();
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        if (selection.isEmpty()) {
            return;
        }
        taskList.cancelEditing();
        List list = selection.toList();
        IMarker[] markers = new IMarker[list.size()];
        list.toArray(markers);

        setClipboard(markers, TaskList.createMarkerReport(markers));

        //Update paste enablement
        taskList.updatePasteEnablement();
    }

    private void setClipboard(IMarker[] markers, String markerReport) {
        try {
            // Place the markers on the clipboard
            Object[] data = new Object[] { markers, markerReport };
            Transfer[] transferTypes = new Transfer[] {
                    MarkerTransfer.getInstance(), TextTransfer.getInstance() };

            // set the clipboard contents
            getTaskList().getClipboard().setContents(data, transferTypes);
        } catch (SWTError e) {
            if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
            if (MessageDialog
                    .openQuestion(
                            getShell(),
                            TaskListMessages.CopyToClipboardProblemDialog_title, TaskListMessages.CopyToClipboardProblemDialog_message)) {
				setClipboard(markers, markerReport);
			}
        }
    }
}

