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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.UpdateMarkersOperation;

class MarkCompletedAction extends TaskAction {

    /**
     * Create a MarkCompletedAction.
     * @param tasklist
     * @param id
     */
    protected MarkCompletedAction(TaskList tasklist, String id) {
        super(tasklist, id);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
                ITaskListHelpContextIds.MARK_COMPLETED_ACTION);
    }

    /**
     * Sets the completed value of the currently selected
     * actions.
     */
    public void run() {
        ISelection selectedMarkers = getTaskList().getSelection();
        if (selectedMarkers instanceof IStructuredSelection) {
            Iterator selections = ((IStructuredSelection) selectedMarkers).iterator();
            ArrayList markers = new ArrayList();
            while (selections.hasNext()) {
                Object marker = selections.next();
                if (marker instanceof IMarker) {
                	markers.add(marker);
                }
            }
    		Map attrs = new HashMap();
    		attrs.put(IMarker.DONE, Boolean.TRUE);
    		IUndoableOperation op = new UpdateMarkersOperation((IMarker [])markers.toArray(new IMarker [markers.size()]), 
    				attrs, getText(), true);
    		execute(op, getText(), null, null);

        }
        
    }

    /**
     * Returns whether this action should be enabled for the given selection.
     * 
     * @param selection the selection
     * @return enablement
     */
    public boolean shouldEnable(IStructuredSelection selection) {
        if (selection.isEmpty()) {
			return false;
		}
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
