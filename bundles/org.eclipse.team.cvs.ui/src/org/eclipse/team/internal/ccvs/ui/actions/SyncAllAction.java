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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.SynchronizeProjectsDialog;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.internal.ui.sync.SyncCompareInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkingSet;

/**
 * Synchronize all CVS projects.
 */
public class SyncAllAction extends SyncAction implements IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow window;
	
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
		this.shell = window.getShell();
	}

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#getSelectedResources()
	 */
	protected IProject[] getSharedProjects() {
		List selected = new ArrayList();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			RepositoryProvider provider = RepositoryProvider.getProvider(projects[i], CVSProviderPlugin.getTypeId());
			if (provider!=null) {
				selected.add(projects[i]);
			}
		}
		return (IProject[]) selected.toArray(new IProject[selected.size()]);
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
		return getSharedProjects();
	}

	protected SyncCompareInput getCompareInput(IResource[] resources) throws CVSException {
		SynchronizeProjectsDialog dialog = new SynchronizeProjectsDialog(getShell());
		if (dialog.open() == IDialogConstants.CANCEL_ID) return null;
		resources = getWorkingSetResources(resources, dialog.getWorkingSet());
		return new CVSSyncCompareInput(resources, dialog.isSyncOutgoingChanges());
	}
	
	/**
	 * 
	 * Return the resources in the working set that are shared with a CVS repository
	 * @param resources
	 * @param set
	 * @return IResource[]
	 */
	private IResource[] getWorkingSetResources(IResource[] resources, IWorkingSet set) throws CVSException {
		// get all the resources out of the working set
		if (set == null) return resources;
		Set sharedResources = new HashSet();
		IAdaptable[] adaptables = set.getElements();
		for (int i = 0; i < adaptables.length; i++) {
			IAdaptable adaptable = adaptables[i];
			Object adapted = adaptable.getAdapter(IResource.class);
			if (adapted != null) {
				IResource resource = ((IResource)adapted);
				if (isSharedWithCVS(resource)) 
					sharedResources.add(resource);
			}
		}
		return (IResource[]) sharedResources.toArray(new IResource[sharedResources.size()]);
	}

	/**
	 * A resource is considered shared 
	 * @param resource
	 * @return boolean
	 */
	private boolean isSharedWithCVS(IResource resource) throws CVSException {
		if (!resource.isAccessible()) return false;
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		if (cvsResource.isManaged()) return true;
		if (!cvsResource.exists()) return false;
		if (cvsResource.isFolder() && ((ICVSFolder) cvsResource).isCVSFolder()) return true;
		if (cvsResource.isIgnored()) return false;
		return cvsResource.getParent().isCVSFolder();
	}

	/**
	 * This is a toolbar action so there are no selected resources.
	 * 
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#getSelectedResources()
	 */
	protected IResource[] getSelectedResources() {
		return new IResource[0];
	}

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#getTargetPage()
	 */
	protected IWorkbenchPage getTargetPage() {
		if (window == null) return super.getTargetPage();
		return window.getActivePage();
	}

}
