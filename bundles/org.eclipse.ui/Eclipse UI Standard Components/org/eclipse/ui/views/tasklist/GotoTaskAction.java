package org.eclipse.ui.views.tasklist;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * This action opens an editor for the resource
 * associated with the selected marker, and
 * jumps to the marker's location in the editor.
 */
/* package */ class GotoTaskAction extends TaskAction {
/**
 * Creates the action.
 */
public GotoTaskAction(TaskList tasklist, String id) {
	super(tasklist, id);
}
/**
 * Performs this action. This action works only for single selection.
 */
public void run() {
	IStructuredSelection selection = (IStructuredSelection) getTaskList().getSelection();
	Object o = selection.getFirstElement();
	if (!(o instanceof IMarker))
		return;
	IMarker marker = (IMarker) o;
	IResource resource = marker.getResource();
	if (!(resource instanceof IFile))
		return;
	IWorkbenchPage page = getTaskList().getSite().getPage();
	try {
		page.openEditor(marker);
	} catch (PartInitException e) {
		DialogUtil.openError(
			page.getWorkbenchWindow().getShell(),
			"Problems Opening Editor",
			e.getMessage(),
			e);
	}
}
}
