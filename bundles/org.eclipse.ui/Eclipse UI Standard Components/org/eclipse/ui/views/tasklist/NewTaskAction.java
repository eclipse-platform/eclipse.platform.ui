package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.help.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * This action creates a new task. If a resource is currently
 * present at the tasklist's input, this task will be
 * associated with it. If the tasklist is currently
 * observing all the markers in the workbench, the task
 * will not be associated with any resource.
 * <p>The newly created task will have low priority,
 * fixed description text and will not be subject to
 * sorting or filtering until its desciprion is being
 * changed for the first time. For this reason, new
 * tasks remain at the top of the task list
 * until modified. It is possible that the newly
 * created task dissapears after that if its
 * type or some other property causes it to
 * be filtered out.
 */
/* package */ class NewTaskAction extends TaskAction {
	
/**
 * Creates the action.
 */
public NewTaskAction(TaskList tasklist, String id) {
	super(tasklist, id);
	WorkbenchHelp.setHelp(this, new Object[] {ITaskListHelpContextIds.NEW_TASK_ACTION});
}
/**
 * Creates a new task marker, sets its priority to LOW,
 * sets the initial description, makes sure that it is
 * always on top until first edited, and opens
 * description field for direct editing.
 */
public void run() {
	if (!verifyVisibility()) return;
	final IMarker[] result = new IMarker[1];
	try {
		getTaskList().getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				TaskList taskList = getTaskList();
				IResource resource = taskList.getWorkspace().getRoot();
				IMarker marker = resource.createMarker(IMarker.TASK);
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
				marker.setAttribute(IMarker.MESSAGE, TaskListMessages.getString("NewTask.enterDescription")); //$NON-NLS-1$
				taskList.setNewlyCreatedTaskInstance(marker);
				result[0] = marker;
			}
		}, null);
	} catch (CoreException e) {
		ErrorDialog.openError(
			getShell(),
			TaskListMessages.getString("NewTask.errorMessage"), //$NON-NLS-1$
			null,
			e.getStatus());
		return;
	}
	// Need to do this in an asyncExec, even though we're in the UI thread here,
	// since the task list updates itself with the addition in an asyncExec,
	// which hasn't been processed yet.
	// Must be done outside IWorkspaceRunnable above since notification for add is
	// sent after IWorkspaceRunnable is run.
	if (result[0] != null) {
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				selectAndEdit(result[0]);
			}
		});
	}
}
/**
 * Selects newly created marker and opens
 * description cell for editing.
 */
private void selectAndEdit(IMarker marker) {
	TaskList taskList = getTaskList();
	taskList.setSelection(new StructuredSelection(marker), true);
	taskList.edit(marker);
}
/**
 * Returns true if the new task will be visible
 * when added and its description changed.
 */
private boolean verifyVisibility() {
	return true;
/*	
	if (getTaskList().showTasks())
		return true;
	// ask the user
	return MessageDialog.openConfirm(
		getShell(), 
		"Question", 
		"The task you are about create will not immediately show up in the task list due to the current filter settings. Do you want to proceed?");
*/		
}
}
