package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.List;

import org.eclipse.core.resources.IMarker;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.MarkerTransfer;


/**
 * Copies a task to the clipboard.
 */
/*package*/ class CopyTaskAction extends TaskAction {
	/**
	 * Creates the action.
	 */
	public CopyTaskAction(TaskList tasklist, String id) {
		super(tasklist, id);
		WorkbenchHelp.setHelp(this, ITaskListHelpContextIds.COPY_TASK_ACTION);
	}
	
	/**
	 * Performs this action.
	 */
	public void run() {
		// Get the selected markers
		TaskList taskList = getTaskList();
		TableViewer viewer = taskList.getTableViewer();
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		if (selection.isEmpty()) {
			return;
		}
		taskList.cancelEditing();
		List list = selection.toList();
		IMarker[] markers = new IMarker[list.size()];
		list.toArray(markers);

		// Place the markers on the clipboard
		Object[] data = new Object[] {
			markers,
			TaskList.createMarkerReport(markers)};				
		Transfer[] transferTypes = new Transfer[] {
			MarkerTransfer.getInstance(),
			TextTransfer.getInstance()};
		taskList.getClipboard().setContents(data, transferTypes);
		
		//Update paste enablement
		taskList.updatePasteEnablement();
	}
}

