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
 * This action opens an editor for the resource
 * associated with the selected marker, and
 * jumps to the marker's location in the editor.
 */
/* package */ class FiltersAction extends TaskAction {
/**
 * Creates the action.
 */
public FiltersAction(TaskList tasklist, String id) {
	super(tasklist, id);
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
