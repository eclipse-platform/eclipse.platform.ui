package org.eclipse.ui.views.tasklist;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import java.util.*;

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
}
/**
 * Fetches all the completed tasks in the workspace and deletes them.
 */
public void run() {
	// Verify.
	if (!MessageDialog
		.openConfirm(
			getShell(), 
			"Question", 
			"Do you want to permanently discard all completed tasks?"
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
		ErrorDialog.openError(getShell(), "Error discarding completed tasks", null, e.getStatus());
	}
}
}
