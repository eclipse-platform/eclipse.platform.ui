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

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.help.WorkbenchHelp;

class MarkCompletedAction extends TaskAction {

    /**
     * Create a MarkCompletedAction.
     * @param tasklist
     * @param id
     */
    protected MarkCompletedAction(TaskList tasklist, String id) {
        super(tasklist, id);
        WorkbenchHelp.setHelp(this,
                ITaskListHelpContextIds.MARK_COMPLETED_ACTION);
    }

    /**
     * Sets the completed value of the currently selected
     * actions.
     */
    public void run() {
        ISelection markers = getTaskList().getSelection();
        if (markers instanceof IStructuredSelection) {
            Iterator selections = ((IStructuredSelection) markers).iterator();
            while (selections.hasNext()) {
                IMarker nextMarker = (IMarker) selections.next();
                getTaskList().setProperty(nextMarker, IMarker.DONE,
                        Boolean.TRUE);
            }
        }
    }

    /**
     * Returns whether this action should be enabled for the given selection.
     */
    public boolean shouldEnable(IStructuredSelection selection) {
        if (selection.isEmpty())
            return false;
        for (Iterator i = selection.iterator(); i.hasNext();) {
            IMarker marker = (IMarker) i.next();
            if (!(MarkerUtil.isMarkerType(marker, IMarker.TASK)
                    && !MarkerUtil.isComplete(marker) && MarkerUtil
                    .isEditable(marker))) {
                return false;
            }
        }
        return true;
    }

}