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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

/**
 * This action opens the properties dialog for the current task.
 */
class TaskPropertiesAction extends TaskAction {

    /**
     * Creates the action.
     * 
     * @param tasklist the task list
     * @param id the id
     */
    public TaskPropertiesAction(TaskList tasklist, String id) {
        super(tasklist, id);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                ITaskListHelpContextIds.TASK_PROPERTIES_ACTION);
    }

    /**
     * Performs this action.
     */
    public void run() {
        IStructuredSelection sel = (IStructuredSelection) getTaskList()
                .getSelection();
        Object o = sel.getFirstElement();
        if (o instanceof IMarker) {
            TaskPropertiesDialog dialog = new TaskPropertiesDialog(getShell());
            dialog.setMarker((IMarker) o);
            dialog.open();
        }
    }
}
