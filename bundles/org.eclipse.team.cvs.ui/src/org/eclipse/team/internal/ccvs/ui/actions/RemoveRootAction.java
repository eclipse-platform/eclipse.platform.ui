package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSProvider;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.DetailsDialogWithProjects;
import org.eclipse.team.internal.ui.actions.TeamAction;

/**
 * RemoveRootAction removes a repository
 */
public class RemoveRootAction extends TeamAction {
	/**
	 * Returns the selected remote files
	 */
	protected ICVSRepositoryLocation[] getSelectedRemoteRoots() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof ICVSRepositoryLocation) {
					resources.add(next);
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(ICVSRepositoryLocation.class);
					if (adapter instanceof ICVSRemoteFile) {
						resources.add(adapter);
						continue;
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			ICVSRepositoryLocation[] result = new ICVSRepositoryLocation[resources.size()];
			resources.toArray(result);
			return result;
		}
		return new ICVSRepositoryLocation[0];
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ICVSRepositoryLocation[] roots = getSelectedRemoteRoots();
				if (roots.length == 0) return;
				ICVSProvider provider = CVSProviderPlugin.getProvider();
				for (int i = 0; i < roots.length; i++) {
					try {
						
						// Check if any projects are shared with the repository
						IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
						final ArrayList shared = new ArrayList();
						for (int j = 0; j < projects.length; j++) {
							RepositoryProvider teamProvider = RepositoryProvider.getProvider(projects[j], CVSProviderPlugin.getTypeId());
							if (teamProvider!=null) {
								CVSTeamProvider cvsProvider = (CVSTeamProvider)teamProvider;
								if (cvsProvider.getCVSWorkspaceRoot().getRemoteLocation().equals(roots[i])) {
									shared.add(projects[j]);
								}
							}
						}
			
						// This will notify the RepositoryManager of the removal
						if (!shared.isEmpty()) {
							Shell shell = getShell();
							final String location = roots[i].getLocation();
							shell.getDisplay().syncExec(new Runnable() {
								public void run() {
									DetailsDialogWithProjects dialog = new DetailsDialogWithProjects(
										getShell(), 
										Policy.bind("RemoteRootAction.Unable_to_Discard_Location_1"), //$NON-NLS-1$
										Policy.bind("RemoteRootAction.Projects_in_the_local_workspace_are_shared_with__2", location), //$NON-NLS-1$
										Policy.bind("RemoteRootAction.The_projects_that_are_shared_with_the_above_repository_are__4"), //$NON-NLS-1$
										(IProject[]) shared.toArray(new IProject[shared.size()]),
										false,
										DetailsDialogWithProjects.DLG_IMG_ERROR);
									dialog.open();
								}
							});
						} else {
							provider.disposeRepository(roots[i]);
						}
					} catch (CVSException e) {
						throw new InvocationTargetException(e);
					}
				}
			}
		}, Policy.bind(Policy.bind("RemoveRootAction.removeRoot_3")), this.PROGRESS_DIALOG); //$NON-NLS-1$

	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRepositoryLocation[] roots = getSelectedRemoteRoots();
		return roots.length > 0;
	}
}

