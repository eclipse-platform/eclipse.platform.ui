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
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariantComparator;
import org.eclipse.team.examples.filesystem.FileSystemPlugin;

public class LocalHistorySubscriber extends Subscriber {

	private long timestamp;
	private static final long LAST_FILESTATE = 0L;
	private LocalHistoryVariantComparator comparator;
	
	public LocalHistorySubscriber() {
		this(LAST_FILESTATE);
	}
	
	public LocalHistorySubscriber(long timestamp) {
		this.timestamp = timestamp;
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
			if(resource.getType() == IResource.FILE) {
				return new IResource[0];
			}
			IContainer container = (IContainer)resource;
			List existingChildren = new ArrayList(Arrays.asList(container.members()));
			existingChildren.addAll(Arrays.asList(container.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, new NullProgressMonitor())));
			return (IResource[]) existingChildren.toArray(new IResource[existingChildren.size()]);
		} catch (CoreException e) {
			FileSystemPlugin.getPlugin().getLog().log(e.getStatus());
			return new IResource[0];
		}
	}

	public IResource[] roots() {
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}

	public SyncInfo getSyncInfo(IResource resource) throws TeamException {
			try {
				if(resource.getType() == IResource.FILE) {
					IFile file = (IFile)resource;
					IFileState[] states = file.getHistory(new NullProgressMonitor());
					if(states.length > 0) {
						// last state only
						SyncInfo info = new SyncInfo(file,null, new LocalHistoryVariant(states[0]), comparator);
						info.init();
						return info;
					} 
				}
			} catch (CoreException e) {
				FileSystemPlugin.getPlugin().getLog().log(e.getStatus());
			}
			return new SyncInfo(resource, null, null, comparator) {
				protected int calculateKind() throws TeamException {
					return IN_SYNC;
				}
			};
	}

	public IResourceVariantComparator getResourceComparator() {
		return comparator;
	}

	public void refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
	}
}
