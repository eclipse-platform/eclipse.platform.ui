package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.MarkerResolutionSelectionDialog;

/**
 * This action displays a list of resolutions for the selected marker
 * 
 * @since 2.0
 */
/* package */ class ResolveMarkerAction extends TaskAction {
	/**
	 * Creates the action.
	 */
	protected ResolveMarkerAction(TaskList tasklist, String id) {
		super(tasklist, id);
	}
	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public boolean isEnabled() {
		IMarker marker = getMarker();
		if (marker == null)
			return false;
		IWorkbench workbench = getTaskList().getViewSite().getWorkbenchWindow().getWorkbench();
		return workbench.getMarkerHelpRegistry().hasResolutions(marker);
	}
	
	/**
	 * Displays a list of resolutions and performs the selection.
	 */
	public void run() {
		IMarker marker = getMarker();
		getTaskList().cancelEditing();
		IMarkerResolution[] resolutions = getResolutions(marker);
		if (resolutions.length == 0) {
			MessageDialog.openInformation(
				getShell(),
				TaskListMessages.getString("Resolve.title"), //$NON-NLS-1 
				TaskListMessages.getString("Resolve.noResolutionsLabel")); //$NON-NLS-1);
			return;
		}	 
		MarkerResolutionSelectionDialog d = new MarkerResolutionSelectionDialog(getShell(), resolutions);
		if (d.open() != d.OK)
			return;
		Object[] result = d.getResult();
		if (result != null && result.length > 0)
			((IMarkerResolution)result[0]).run(marker);			
	}
	
	/**
	 * Returns the resolutions for the given marker.
	 *
	 * @param the marker for which to obtain resolutions
	 * @return the resolutions for the selected marker	
	 */
	private IMarkerResolution[] getResolutions(IMarker marker) {
		IWorkbench workbench = getTaskList().getViewSite().getWorkbenchWindow().getWorkbench();
		return workbench.getMarkerHelpRegistry().getResolutions(marker);
	}

	/**
	 * Returns the selected marker (may be <code>null</code>).
	 * 
	 * @return the selected marker
	 */
	private IMarker getMarker() {
		IStructuredSelection selection = (IStructuredSelection)getTaskList().getSelection();
		// only enable for single selection
		if (selection.size() != 1)
			return null;
		return (IMarker)selection.getFirstElement();
	}
}
