package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.HistoryView;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.actions.TeamAction;

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
		}, Policy.bind("ShowHistoryAction.showHistory"), this.PROGRESS_BUSYCURSOR);
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
		ICVSResource cvsResource = new LocalFile(resource.getLocation().toFile());
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