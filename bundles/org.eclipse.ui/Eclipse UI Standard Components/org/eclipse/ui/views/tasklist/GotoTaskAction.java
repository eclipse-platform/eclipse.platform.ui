package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.OpenStrategy;
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
	WorkbenchHelp.setHelp(this, ITaskListHelpContextIds.GOTO_TASK_ACTION);
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
	if (marker.exists() && resource instanceof IFile) {
		IWorkbenchPage page = getTaskList().getSite().getPage();
		try {
			page.openEditor(marker,OpenStrategy.activateOnOpen());
		} catch (PartInitException e) {
			DialogUtil.openError(
				page.getWorkbenchWindow().getShell(),
				TaskListMessages.getString("GotoTask.errorMessage"), //$NON-NLS-1$
				e.getMessage(),
				e);
		}
	}
}
}
