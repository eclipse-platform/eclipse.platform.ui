/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.localstore;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
import java.util.Enumeration;
import java.util.List;

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
protected void deleteAndKeepHistory(UnifiedTreeNode node) {
	String location = node.getLocalLocation();
	Resource target = (Resource) node.getResource();
	try {
		java.io.File localFile = new java.io.File(location);
		if (target.getType() == IResource.FOLDER) {
			for (Enumeration children = node.getChildren(); children.hasMoreElements();)
				deleteAndKeepHistory((UnifiedTreeNode) children.nextElement());
			node.removeChildrenFromTree();
			delete(target, localFile);
			return;
		}
		HistoryStore store = target.getLocalManager().getHistoryStore();
		store.addState(target.getFullPath(), localFile, node.getLastModified(), true);
		if (localFile.exists())
			delete(target, localFile);
		else
			target.deleteResource(convertToPhantom, status);
	} catch (CoreException e) {
		status.add(e.getStatus());
	}
}
protected void delete(Resource target, java.io.File localFile) {
	if(!target.getLocalManager().getStore().delete(localFile, status))
		return;
	try {
		target.deleteResource(convertToPhantom, status);
	} catch (CoreException e) {
		status.add(e.getStatus());
	}
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
		if (keepHistory)
			deleteAndKeepHistory(node);
		else
			delete(target, new java.io.File(node.getLocalLocation()));
		return false;
	} finally {
		monitor.worked(1);
	}
}
}