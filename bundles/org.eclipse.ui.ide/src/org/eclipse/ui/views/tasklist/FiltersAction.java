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

import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This action opens an editor for the resource
 * associated with the selected marker, and
 * jumps to the marker's location in the editor.
 */
class FiltersAction extends TaskAction {

    /**
     * Creates the action.
     */
    public FiltersAction(TaskList tasklist, String id) {
        super(tasklist, id);
        WorkbenchHelp.setHelp(this, ITaskListHelpContextIds.FILTERS_ACTION);
    }

    /**
     * Performs this action.
     */
    public void run() {
        FiltersDialog dialog = new FiltersDialog(getShell());
        TasksFilter filter = getTaskList().getFilter();
        dialog.setFilter(filter);
        int result = dialog.open();
        if (result == FiltersDialog.OK) {
            getTaskList().filterChanged();
        }
    }

}