package org.eclipse.core.internal.localstore;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
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
	protected FileSystemStore localStore;

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
	status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, Policy.bind("deleteProblem", null), null);
}
protected void delete(UnifiedTreeNode node) {
	IPath location = node.getLocalLocation();
	Resource target = (Resource) node.getResource();
	try {
		if (keepHistory) {
			if (target.getType() == IResource.FOLDER) {
				for (Enumeration children = node.getChildren(); children.hasMoreElements();)
					delete((UnifiedTreeNode) children.nextElement());
				node.removeChildrenFromTree();
				if(!getStore().delete(location.toFile(), status))
					return;
			} else {
				target.getLocalManager().getHistoryStore().addState(target.getFullPath(), location, node.getLastModified(), true);
				if (target.getLocation().toFile().exists()) {
					String message = "Could not delete file: " + target.getFullPath();
					status.add(new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, target.getFullPath(), message));
					return;
				}
			}
		} else {
			if(!getStore().delete(location.toFile(), status))
				return;
		}
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
protected FileSystemStore getStore() {
	if (localStore == null)
		localStore = new FileSystemStore();
	return localStore;
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
		if (shouldSkip(target)) {
			removeFromSkipList(target);
			int ticks = target.countResources(IResource.DEPTH_INFINITE, false);
			monitor.worked(ticks);
			return false;
		}
		if (isAncestorOfResourceToSkip(target))
			return true;
		delete(node);
		return false;
	} finally {
		monitor.worked(1);
	}
}
}
