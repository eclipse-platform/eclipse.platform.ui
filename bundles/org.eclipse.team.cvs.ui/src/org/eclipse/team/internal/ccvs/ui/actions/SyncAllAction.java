/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.AdaptableResourceList;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.ProjectSelectionDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Synchronize all CVS projects.
 */
public class SyncAllAction extends SyncAction implements IWorkbenchWindowActionDelegate {
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.shell = window.getShell();
	}

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#getSelectedResources()
	 */
	protected IResource[] getSharedProjects() {
		List selected = new ArrayList();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			RepositoryProvider provider = RepositoryProvider.getProvider(projects[i], CVSProviderPlugin.getTypeId());
			if (provider!=null) {
				selected.add(projects[i]);
			}
		}
		return (IResource[]) selected.toArray(new IResource[selected.size()]);
	}

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return getSharedProjects().length > 0;
	}

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#getSelectedResources()
	 */
	protected IResource[] getResourcesToSync() {
		IResource[] resources = getSharedProjects();
		
		ProjectSelectionDialog dialog = new ProjectSelectionDialog	(getShell(), 
			new AdaptableResourceList(resources), 
			new WorkbenchContentProvider(), new WorkbenchLabelProvider(), 
			Policy.bind("SyncAllAction.selectProjects"));
		dialog.setInitialSelections(resources);
		if (dialog.open() == IDialogConstants.CANCEL_ID) return new IResource[0];
		Object[] result = dialog.getResult();
		IResource[] selectedResources = new IResource[result.length];
		for (int i = 0; i < result.length; i++) {
			selectedResources[i] = (IResource)result[i];
		}
		return selectedResources;
	}

	/**
	 * This is a toolbar action so there are no selected resources.
	 * 
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#getSelectedResources()
	 */
	protected IResource[] getSelectedResources() {
		return new IResource[0];
	}

}
