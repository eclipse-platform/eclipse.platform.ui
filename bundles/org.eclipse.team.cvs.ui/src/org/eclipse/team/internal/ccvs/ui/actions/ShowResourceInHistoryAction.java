package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.HistoryView;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.actions.TeamAction;

public class ShowResourceInHistoryAction extends TeamAction {
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				IResource[] resources = getSelectedResources();
				if (resources.length != 1) return;
				HistoryView view = HistoryView.openInActivePerspective();
				if (view != null) {
					view.showHistory(resources[0]);
				}
			}
		}, Policy.bind("ShowHistoryAction.showHistory"), this.PROGRESS_BUSYCURSOR); //$NON-NLS-1$
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		// Show Resource In History is enabled for resources which have been committed.
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return false;
		IResource resource = resources[0];
		if (!(resource instanceof IFile)) return false;
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		if (!cvsResource.isManaged()) return false;
		if (cvsResource.getSyncInfo().isAdded()) return false;
		return true;
	}
	/** (Non-javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			//this action can be invoked by double-click, in which case
			//there is no target action
			if (action != null) {
				try {
					action.setEnabled(isEnabled());
				} catch (TeamException e) {
					action.setEnabled(false);
					handle(e, null, null);
				}
			}
		}
	}
}