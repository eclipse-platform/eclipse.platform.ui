/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.util.Enumeration;
import java.util.List;
import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class DeleteVisitor implements IUnifiedTreeVisitor, ICoreConstants {
	protected IProgressMonitor monitor;
	protected boolean force;
	protected boolean keepHistory;
	protected MultiStatus status;
	protected List skipList;

	/**
	 * Flag to indicate if resources are going to be removed
	 * from the workspace or converted into phantoms
	 */
	protected boolean convertToPhantom;

	public DeleteVisitor(List skipList, boolean force, boolean convertToPhantom, boolean keepHistory, IProgressMonitor monitor) {
		this.skipList = skipList;
		this.force = force;
		this.convertToPhantom = convertToPhantom;
		this.keepHistory = keepHistory;
		this.monitor = monitor;
		status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_DELETE_LOCAL, Policy.bind("localstore.deleteProblem"), null); //$NON-NLS-1$
	}

	/**
	 * Deletes a file from both the workspace resource tree and the file system.
	 */
	protected void delete(UnifiedTreeNode node, boolean deleteLocalFile, boolean shouldKeepHistory) {
		Resource target = (Resource) node.getResource();
		try {
			deleteLocalFile = deleteLocalFile && !target.isLinked() && node.existsInFileSystem();
			java.io.File localFile = deleteLocalFile ? new java.io.File(node.getLocalLocation()) : null;
			// if it is a folder in the file system, delete its children first
			if (target.getType() == IResource.FOLDER) {
				// if this file is a POSIX symbolic link then deleting the local file before the recursion will
				// keep its contents from being deleted on the filesystem.
				if (localFile != null)
					localFile.delete();
				for (Enumeration children = node.getChildren(); children.hasMoreElements();)
					delete((UnifiedTreeNode) children.nextElement(), deleteLocalFile, shouldKeepHistory);
				node.removeChildrenFromTree();
				delete(node.existsInWorkspace() ? target : null, localFile);
				return;
			}
			if (shouldKeepHistory) {
				IHistoryStore store = target.getLocalManager().getHistoryStore();
				store.addState(target.getFullPath(), localFile, node.getLastModified(), true);
			}
			delete(node.existsInWorkspace() ? target : null, localFile);
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}

	//XXX: in which situation would delete be called with (null, null)? It happens (see bug 29445), but why?
	protected void delete(Resource target, java.io.File localFile) {
		if (target != null) {
			try {
				if (localFile != null && !target.isLinked())
					target.getLocalManager().getStore().delete(localFile);
				target.deleteResource(convertToPhantom, status);
			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		} else if (localFile != null)
			localFile.delete();
	}

	protected boolean equals(IResource one, IResource another) throws CoreException {
		return one.getFullPath().equals(another.getFullPath());
	}

	public MultiStatus getStatus() {
		return status;
	}

	protected boolean isAncestor(IResource one, IResource another) throws CoreException {
		return one.getFullPath().isPrefixOf(another.getFullPath()) && !equals(one, another);
	}

	protected boolean isAncestorOfResourceToSkip(IResource resource) throws CoreException {
		if (skipList == null)
			return false;
		for (int i = 0; i < skipList.size(); i++) {
			IResource target = (IResource) skipList.get(i);
			if (isAncestor(resource, target))
				return true;
		}
		return false;
	}

	protected void removeFromSkipList(IResource resource) {
		if (skipList != null)
			skipList.remove(resource);
	}

	protected boolean shouldSkip(IResource resource) throws CoreException {
		if (skipList == null)
			return false;
		for (int i = 0; i < skipList.size(); i++)
			if (equals(resource, (IResource) skipList.get(i)))
				return true;
		return false;
	}

	public boolean visit(UnifiedTreeNode node) throws CoreException {
		Policy.checkCanceled(monitor);
		Resource target = (Resource) node.getResource();
		try {
			if (target.getType() == IResource.PROJECT)
				return true;
			if (shouldSkip(target)) {
				removeFromSkipList(target);
				int ticks = target.countResources(IResource.DEPTH_INFINITE, false);
				monitor.worked(ticks);
				return false;
			}
			if (isAncestorOfResourceToSkip(target))
				return true;

			delete(node, true, keepHistory);
			return false;
		} finally {
			monitor.worked(1);
		}
	}
}