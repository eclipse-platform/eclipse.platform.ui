package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
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
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.HistoryView;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.actions.TeamAction;

public class ShowResourceInHistoryAction extends TeamAction {
	/**
	 * Returns the selected remote file
	 */
	protected ICVSRemoteFile getSelectedRemoteFile() {
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return null;
		if (!(resources[0] instanceof IFile)) return null;
		IFile file = (IFile)resources[0];
		CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(file.getProject());
		try {
			return (ICVSRemoteFile)provider.getRemoteResource(file);
		} catch (TeamException e) {
			CVSUIPlugin.log(e.getStatus());
			return null;
		}
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ICVSRemoteFile file = getSelectedRemoteFile();
				if (file == null) {
					// No history for selected file
					MessageDialog.openWarning(getShell(), Policy.bind("ShowHistoryAction.noHistory"), Policy.bind("ShowHistoryAction.noHistoryLong"));
					return;
				}
				HistoryView view = HistoryView.openInActivePerspective();
				if (view != null) {
					view.showHistory(file);
				}
			}
		}, Policy.bind("ShowHistoryAction.showHistory"), this.PROGRESS_BUSYCURSOR);
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		return resources.length == 1;
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
				}
			}
		}
	}
}