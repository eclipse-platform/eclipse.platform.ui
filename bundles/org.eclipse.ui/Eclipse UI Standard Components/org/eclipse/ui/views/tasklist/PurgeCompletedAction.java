package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This action deletes all the tasks found in the registry that
 * are marked as completed.
 */
/* package */ class PurgeCompletedAction extends TaskAction {
/**
 * Creates the action.
 */
public PurgeCompletedAction(TaskList tasklist, String id) {
	super(tasklist, id);
	WorkbenchHelp.setHelp(this, ITaskListHelpContextIds.PURGE_COMPLETED_TASK_ACTION);
}
/**
 * Fetches all the completed tasks in the workspace and deletes them.
 */
public void run() {
	// Verify.
	if (!MessageDialog
		.openConfirm(
			getShell(), 
			TaskListMessages.getString("PurgeCompleted.question"),  //$NON-NLS-1$
			TaskListMessages.getString("PurgeCompleted.permanent") //$NON-NLS-1$
			))
		return;

	try {
		IResource resource = getTaskList().getResource();
		int depth = getTaskList().getResourceDepth();
		IMarker[] tasks = resource.findMarkers(IMarker.TASK, true, depth);
		final List completed = new ArrayList(tasks.length);
		for (int i = 0; i < tasks.length; i++) {
			IMarker task = tasks[i];
			if (MarkerUtil.isComplete(task)) {
				completed.add(task);
			}
		}
		// Check if there is anything to do
		if (completed.size() == 0)
			return;
		IMarker[] toDelete = new IMarker[completed.size()];
		completed.toArray(toDelete);
		getTaskList().getWorkspace().deleteMarkers(toDelete);
	} catch (CoreException e) {
		ErrorDialog.openError(getShell(), TaskListMessages.getString("PurgeCompleted.errorMessage"), null, e.getStatus()); //$NON-NLS-1$
	}
}
}
