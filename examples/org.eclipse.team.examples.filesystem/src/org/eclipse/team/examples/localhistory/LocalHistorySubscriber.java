/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.localhistory;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

public class LocalHistorySubscriber extends Subscriber {

	private LocalHistoryVariantComparator comparator;
	
	public LocalHistorySubscriber() {
		this.comparator = new LocalHistoryVariantComparator();
	}
	
	public String getName() {
		return "Local History Subscriber"; //$NON-NLS-1$
	}

	public boolean isSupervised(IResource resource) throws TeamException {
		// all resources in the workspace can potentially have resource history
		return true;
	}

	public IResource[] members(IResource resource) throws TeamException {
		try {
			if(resource.getType() == IResource.FILE)
				return new IResource[0];
			IContainer container = (IContainer)resource;
			List existingChildren = new ArrayList(Arrays.asList(container.members()));
			existingChildren.addAll(Arrays.asList(container.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, null)));
			return (IResource[]) existingChildren.toArray(new IResource[existingChildren.size()]);
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}

	public IResource[] roots() {
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}

	public SyncInfo getSyncInfo(IResource resource) throws TeamException {
		try {
			IResourceVariant variant = null;
			if(resource.getType() == IResource.FILE) {
				IFile file = (IFile)resource;
				IFileState[] states = file.getHistory(null);
				if(states.length > 0) {
					// last state only
					variant = new LocalHistoryVariant(states[0]);
				} 
			}
			SyncInfo info = new LocalHistorySyncInfo(resource, variant, comparator);
			info.init();
			return info;
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}

	public IResourceVariantComparator getResourceComparator() {
		return comparator;
	}

	public void refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
	}
}
