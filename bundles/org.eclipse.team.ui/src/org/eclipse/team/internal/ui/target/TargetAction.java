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
package org.eclipse.team.internal.ui.target;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.actions.TeamAction;

public abstract class TargetAction extends TeamAction {
	/**
	 * Get selected remote target folders
	 */
	protected IRemoteTargetResource[] getSelectedRemoteFolders() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				IRemoteTargetResource remote = null;
				if (next instanceof RemoteResourceElement) {
					remote = ((RemoteResourceElement)next).getRemoteResource();
				} else if(next instanceof SiteElement) {
					try {
						remote = ((SiteElement)next).getSite().getRemoteResource();
					} catch (TeamException e) {
						TeamUIPlugin.handle(e);
						return new IRemoteTargetResource[0];
					}
				}
				if(remote != null && remote.isContainer()) {
					resources.add(remote);
					continue;
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			return (IRemoteTargetResource[])resources.toArray(new IRemoteTargetResource[resources.size()]);
		}
		return new IRemoteTargetResource[0];
	}
	
	/**
	 * Get selected remote target folders
	 */
	protected Site[] getSelectedSites() {
		ArrayList sites = new ArrayList();;
		if (!selection.isEmpty()) {
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				IRemoteTargetResource remote = null;
				if (next instanceof SiteElement) {
					sites.add(((SiteElement)next).getSite());
				}
			}
		}
		return (Site[])sites.toArray(new Site[sites.size()]);
	}
	
	protected IResource[] findResourcesWithOutgoingChanges(IResource[] resources) throws TeamException, CoreException {
		// Collect the dirty resource		
		final List dirtyResources = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			final TargetProvider provider = TargetManager.getProvider(resource.getProject());
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (resource.getType() == IResource.FILE) {
						if (provider.isDirty(resource) || ! provider.hasBase(resource)) {
							dirtyResources.add(resource);
						}
					} else {
						// Check for outgoing folder deletions?
					}
					return true;
				}
			}, IResource.DEPTH_INFINITE, true /* include phantoms */);		
		}
		return (IResource[]) dirtyResources.toArray(new IResource[dirtyResources.size()]);
	}
}
