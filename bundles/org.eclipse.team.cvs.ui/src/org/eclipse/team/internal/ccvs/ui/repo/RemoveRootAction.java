/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.actions.CVSAction;
import org.eclipse.team.internal.ui.DetailsDialogWithProjects;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;


/**
 * RemoveRootAction removes a repository
 */
public class RemoveRootAction extends SelectionListenerAction {
	private IStructuredSelection selection;
	private Shell shell;
	
	public RemoveRootAction(Shell shell) {
		super(Policy.bind("RemoteRootAction.label")); //$NON-NLS-1$
		this.shell = shell;
	}
	
	/**
	 * Returns the selected remote files
	 */
	protected ICVSRepositoryLocation[] getSelectedRemoteRoots() {
		ArrayList resources = null;
		if (selection!=null && !selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = CVSAction.getAdapter(elements.next(), ICVSRepositoryLocation.class);
				if (next instanceof ICVSRepositoryLocation) {
					resources.add(next);
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
	
	protected String getErrorTitle() {
		return Policy.bind("RemoveRootAction.removeRoot_3"); //$NON-NLS-1$
	}

	public void run() {
		ICVSRepositoryLocation[] roots = getSelectedRemoteRoots();
		if (roots.length == 0) return;
		CVSProviderPlugin provider = CVSProviderPlugin.getPlugin();
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
					final String location = roots[i].getLocation();
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							DetailsDialogWithProjects dialog = new DetailsDialogWithProjects(
								shell, 
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
				CVSUIPlugin.log(e);
			}
		}
	}

	protected boolean updateSelection(IStructuredSelection selection) {
		this.selection = selection;

		ICVSRepositoryLocation[] roots = getSelectedRemoteRoots();
		return roots.length > 0;
	}

}

