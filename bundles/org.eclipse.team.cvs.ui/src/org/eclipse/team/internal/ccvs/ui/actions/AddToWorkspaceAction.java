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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.IRemoteFolder;
import org.eclipse.team.ccvs.core.IRemoteRoot;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * Add some remote resources to the workspace. Current implementation:
 * -Works only for remote folders
 * -Does not prompt for project name; uses folder name instead
 */
public class AddToWorkspaceAction extends TeamAction {
	/**
	 * Returns the selected remote folders
	 */
	protected IRemoteFolder[] getSelectedRemoteFolders() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof IRemoteFolder) {
					resources.add(next);
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(IRemoteFolder.class);
					if (adapter instanceof IRemoteFolder) {
						resources.add(adapter);
						continue;
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			IRemoteFolder[] result = new IRemoteFolder[resources.size()];
			resources.toArray(result);
			return result;
		}
		return new IRemoteFolder[0];
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					IRemoteFolder[] folders = getSelectedRemoteFolders();
					IProject[] projects = new IProject[folders.length];
					for (int i = 0; i < folders.length; i++) {
						String name = folders[i].getName();
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
						if (project.exists()) {
							// Make sure the user understands they will overwrite the project.
						}
						projects[i] = project;
					}
					CVSTeamProvider.checkout(folders, projects, monitor);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("AddToWorkspaceAction.add"), this.PROGRESS_DIALOG);
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IRemoteFolder[] resources = getSelectedRemoteFolders();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] instanceof IRemoteRoot) return false;
		}
		return true;
	}
}