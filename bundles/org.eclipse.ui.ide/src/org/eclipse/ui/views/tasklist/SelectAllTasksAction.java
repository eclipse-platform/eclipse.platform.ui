/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.tasklist;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.PlatformUI;

/**
 * This action selects all tasks currently showing in the task list.
 */
class SelectAllTasksAction extends TaskAction {

    /**
     * Creates the action.
     */
    protected SelectAllTasksAction(TaskList tasklist, String id) {
        super(tasklist, id);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                ITaskListHelpContextIds.SELECT_ALL_TASKS_ACTION);
    }

    /**
     * Selects all resources in the view.
     */
    public void run() {
        getTaskList().cancelEditing();
        TableViewer viewer = getTaskList().getTableViewer();
        viewer.getTable().selectAll();
        // force viewer selection change
        viewer.setSelection(viewer.getSelection());
    }
}
