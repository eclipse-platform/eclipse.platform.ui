package org.eclipse.ui.views.tasklist;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.help.WorkbenchHelp;

class MarkCompletedAction extends TaskAction {

	/**
	 * Create a MarkCompletedAction.
	 * @param tasklist
	 * @param id
	 */
	protected MarkCompletedAction(TaskList tasklist, String id) {
		super(tasklist, id);
		WorkbenchHelp.setHelp(this, ITaskListHelpContextIds.MARK_COMPLETED_ACTION);
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
				getTaskList().setProperty(nextMarker, IMarker.DONE, Boolean.TRUE);
			}
		}
	}

	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public boolean isEnabled() {
		ISelection markers = getTaskList().getSelection();
		if (markers instanceof IStructuredSelection) {
			Iterator selections = ((IStructuredSelection) markers).iterator();
			
			//Do not enable if there is no selection
			if(!selections.hasNext())
				return false;
			while (selections.hasNext()) {
				IMarker marker = (IMarker) selections.next();
				if (!MarkerUtil.isMarkerType(marker, IMarker.TASK)
					|| MarkerUtil.isComplete(marker))
					return false;
			}
		}
		return true;
	}

}