package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This action opens the properties dialog for the current task.
 */
/* package */ class TaskPropertiesAction extends TaskAction {
	
/**
 * Creates the action.
 */
public TaskPropertiesAction(TaskList tasklist, String id) {
	super(tasklist, id);
}

/**
 * Performs this action.
 */
public void run() {
	IStructuredSelection sel = (IStructuredSelection) getTaskList().getSelection();
	Object o = sel.getFirstElement();
	if (o instanceof IMarker) {
		IMarker marker = (IMarker) o;
		TaskPropertiesDialog dialog = new TaskPropertiesDialog(getTaskList(), marker);
		dialog.open();
	}
}

}
