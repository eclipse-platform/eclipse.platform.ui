/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.ui.views.tasklist;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

class MarkCompletedAction extends TaskAction {

	/**
	 * Create a MarkCompletedAction.
	 * @param tasklist
	 * @param id
	 */
	protected MarkCompletedAction(TaskList tasklist, String id) {
		super(tasklist, id);
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