package org.eclipse.ui.views.tasklist;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
	TasksFilter oldFilter = (TasksFilter) filter.clone();
	dialog.setFilter(filter);
	int result = dialog.open();
	if (result == FiltersDialog.OK && !oldFilter.equals(dialog.getFilter())) {
		getTaskList().filterChanged();
	}
}
}
