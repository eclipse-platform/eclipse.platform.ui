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

import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

/**
 * This action opens an editor for the resource
 * associated with the selected marker, and
 * jumps to the marker's location in the editor.
 */
class FiltersAction extends TaskAction {

    /**
     * Creates the action.
     * @param tasklist the task list
     * @param id the id
     */
    public FiltersAction(TaskList tasklist, String id) {
        super(tasklist, id);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				ITaskListHelpContextIds.FILTERS_ACTION);
    }

    /**
     * Performs this action.
     */
    public void run() {
        FiltersDialog dialog = new FiltersDialog(getShell());
        TasksFilter filter = getTaskList().getFilter();
        dialog.setFilter(filter);
        int result = dialog.open();
        if (result == Window.OK) {
            getTaskList().filterChanged();
        }
    }

}
