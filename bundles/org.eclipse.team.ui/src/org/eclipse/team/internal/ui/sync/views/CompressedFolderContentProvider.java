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
package org.eclipse.team.internal.ui.sync.views;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.sync.sets.SubscriberInput;
import org.eclipse.team.internal.ui.sync.sets.SyncSetChangedEvent;

/**
 * The contents provider compressed in-sync folder paths
 */
public class CompressedFolderContentProvider extends SyncSetTreeContentProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.views.SyncSetContentProvider#handleResourceAdditions(org.eclipse.team.internal.ui.sync.views.SyncSetChangedEvent)
	 */
	protected void handleResourceAdditions(SyncSetChangedEvent event) {
		AbstractTreeViewer tree = getTreeViewer();
		if (tree != null) {
			// TODO: For now, refresh any projects with additions
			IResource[] roots = event.getAddedRoots();
			refreshProjects(tree, roots);
		} else {
			super.handleResourceAdditions(event);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.views.SyncSetContentProvider#handleResourceRemovals(org.eclipse.team.internal.ui.sync.views.SyncSetChangedEvent)
	 */
	protected void handleResourceRemovals(SyncSetChangedEvent event) {
		AbstractTreeViewer tree = getTreeViewer();
		if (tree != null) {
			// TODO: For now, refresh any projects with deletions
			IResource[] roots = event.getRemovedRoots();
			refreshProjects(tree, roots);
		} else {
			super.handleResourceRemovals(event);
		}
	}

	private void refreshProjects(AbstractTreeViewer tree, IResource[] roots) {
		if (roots.length == 0) return;
		Set projects = new HashSet();
		for (int i = 0; i < roots.length; i++) {
			if (roots[i].getType() == IResource.PROJECT) {
				// when a project is involved, refresh the whole tree
				tree.refresh();
				return;
			}
			projects.add(getModelObject(roots[i].getProject()));
		}
		for (Iterator iter = projects.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			tree.refresh(element);
		}
	}
	
	public Object getParent(Object element) {
		if (element instanceof CompressedFolder) {
			// The parent of a compressed folder is always the project
			return getModelObject(getResource(element).getProject());
		}
		Object parent = super.getParent(element);
		if (parent instanceof SynchronizeViewNode) {
			SyncInfo info = ((SynchronizeViewNode)parent).getSyncInfo();
			if (info == null) {
				// The resource is in-sync so return a compressed folder
				IResource resource = ((SynchronizeViewNode)parent).getResource();
				if (resource.getType() == IResource.FOLDER) {
					return new CompressedFolder((SubscriberInput)viewer.getInput(), resource);
					
				}
			}
		}
		return parent;
	}

	public Object[] getChildren(Object element) {
		IResource resource = getResource(element);
		if (resource != null) {
			if (resource.getType() == IResource.PROJECT) {
				return getProjectChildren((IProject)resource);
			} else if (resource.getType() == IResource.FOLDER) {
				return getFolderChildren(resource);
			}
		}
		return super.getChildren(element);
	}

	private Object[] getFolderChildren(IResource resource) {
		// Folders will only contain out-of-sync children
		IResource[] children = getSyncSet().members(resource);
		List result = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			IResource child = children[i];
			SyncInfo info = getSyncSet().getSyncInfo(child);
			if (info != null) {
				result.add(getModelObject(info.getLocal()));
			}
		}
		return (Object[]) result.toArray(new Object[result.size()]);
	}

	private Object[] getProjectChildren(IProject project) {
		SyncInfo[] outOfSync = getSyncSet().getOutOfSyncDescendants(project);
		Set result = new HashSet();
		for (int i = 0; i < outOfSync.length; i++) {
			SyncInfo info = outOfSync[i];
			IResource local = info.getLocal();
			if (local.getProjectRelativePath().segmentCount() == 1) {
				// If the resource is a child of the project, include it uncompressed
				result.add(getModelObject(local));
			} else {
				IContainer container = getLowestInSyncParent(local);
				if (container.getType() == IResource.FOLDER) {
					result.add(getModelObject(container));
				}
			}
		}
		return (Object[]) result.toArray(new Object[result.size()]);
	}

	/**
	 * Return a compressed folder if the provided resource is an in-sync folder.
	 * Warning: This method will return a compressed folder for any in-sync
	 * folder, even those that do not contain out-of-sync resources (i.e. those that
	 * are not visible in the view).
	 */
	public Object getModelObject(IResource resource) {
		if (resource.getType() == IResource.FOLDER && getSyncSet().getSyncInfo(resource) == null) {
			return new CompressedFolder(getSubscriberInput(), resource);
		}
		return super.getModelObject(resource);
	}
	
	private IContainer getLowestInSyncParent(IResource resource) {
		if (resource.getType() == IResource.ROOT) return (IContainer)resource;
		IContainer parent = resource.getParent();
		if (getSyncSet().getSyncInfo(parent) == null) {
			return parent;
		}
		return getLowestInSyncParent(parent);
	}
	
}
