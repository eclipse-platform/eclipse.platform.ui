package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.MarkerResolutionSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

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
		if (getMarker() == null)
			return false;
		IWorkbench workbench = getTaskList().getViewSite().getWorkbenchWindow().getWorkbench();
		return workbench.getMarkerHelpRegistry().hasResolutions(getMarker());
	}
	
	/**
	 * Displays a list of resolutions and performs the selection.
	 */
	public void run() {
		getTaskList().cancelEditing();
		MarkerResolutionSelectionDialog d = new MarkerResolutionSelectionDialog(getShell(), getResolutions());
		if (d.open() != d.OK)
			return;
		Object[] result = d.getResult();
		if (result != null && result.length > 0)
			((IMarkerResolution)result[0]).run();			
	}
	
	/**
	 * Returns the resolutions for the selected marker.
	 *
	 * @return the resolutions for the selected marker	
	 */
	private IMarkerResolution[] getResolutions() {
		if (getMarker() == null)
			return new IMarkerResolution[0];
		IWorkbench workbench = getTaskList().getViewSite().getWorkbenchWindow().getWorkbench();
		return workbench.getMarkerHelpRegistry().getResolutions(getMarker());
	}

	/**
	 * Returns the selected marker (may be <code>null</code>).
	 * 
	 * @return the selected marker
	 */
	private IMarker getMarker() {
		return (IMarker)((IStructuredSelection)getTaskList().getSelection()).getFirstElement();
	}
}
