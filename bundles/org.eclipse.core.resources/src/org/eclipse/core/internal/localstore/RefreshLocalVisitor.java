package org.eclipse.core.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
//
public class RefreshLocalVisitor implements IUnifiedTreeVisitor, ILocalStoreConstants {
	protected IProgressMonitor monitor;
	protected Workspace workspace;
	protected boolean resourceChanged;

	/** control constants */
	protected static final int RL_UNKNOWN 		= 0;
	protected static final int RL_IN_SYNC 		= 1;
	protected static final int RL_NOT_IN_SYNC 	= 2;

public RefreshLocalVisitor(IProgressMonitor monitor) {
	this.monitor = monitor;
	workspace = (Workspace) ResourcesPlugin.getWorkspace();
	resourceChanged = false;
}
/**
 * This method has the same implementation as resourceChanged but as they are different
 * cases, we prefer to use different methods.
 */
protected void contentAdded(Resource target, long lastModified) throws CoreException {
	resourceChanged(target, lastModified);
}
protected void createResource(UnifiedTreeNode node, Resource target) throws CoreException {
	ResourceInfo info = target.getResourceInfo(false, false);
	int flags = target.getFlags(info);
	if (target.exists(flags, false))
		return;
	/* make sure target's parent exists */
	if (node.getLevel() == 0) {
		IContainer parent = target.getParent();
		if (parent.getType() == IResource.FOLDER)
			 ((Folder) target.getParent()).ensureExists(monitor);
	}
	/* Use the basic file creation protocol since we don't want to create any content on disk. */
	workspace.createResource(target, false);
	info = target.getResourceInfo(false, true);
	target.getLocalManager().updateLocalSync(info, node.getLastModified(), target.getType() == IResource.FILE);
}
protected void deleteResource(UnifiedTreeNode node, Resource target) throws CoreException {
	ResourceInfo info = target.getResourceInfo(false, false);
	int flags = target.getFlags(info);
	if (target.exists(flags, false))
		target.deleteResource(true, null);
	node.setExistsWorkspace(false);
}
protected void fileToFolder(UnifiedTreeNode node, Resource target) throws CoreException {
	ResourceInfo info = target.getResourceInfo(false, false);
	int flags = target.getFlags(info);
	if (target.exists(flags, true)) {
		target = (Folder) ((File) target).changeToFolder();
	} else {
		if (!target.exists(flags, false)) {
			target = (Resource) workspace.getRoot().getFolder(target.getFullPath());
			// Use the basic file creation protocol since we don't want to create any content on disk.
			workspace.createResource(target, false);
		}
	}
	node.setResource(target);
	info = target.getResourceInfo(false, true);
	target.getLocalManager().updateLocalSync(info, node.getLastModified(), false);
}
protected void folderToFile(UnifiedTreeNode node, Resource target) throws CoreException {
	ResourceInfo info = target.getResourceInfo(false, false);
	int flags = target.getFlags(info);
	if (target.exists(flags, true))
		target = (File) ((Folder) target).changeToFile();
	else {
		if (!target.exists(flags, false)) {
			target = (Resource) workspace.getRoot().getFile(target.getFullPath());
			// Use the basic file creation protocol since we don't want to 
			// create any content on disk.
			workspace.createResource(target, false);
		}
	}
	node.setResource(target);
	info = target.getResourceInfo(false, true);
	target.getLocalManager().updateLocalSync(info, node.getLastModified(), true);
}
protected void resourceChanged(Resource target, long lastModified) throws CoreException {
	ResourceInfo info = target.getResourceInfo(false, true);
	if (info == null)
		return;
	target.getLocalManager().updateLocalSync(info, lastModified, target.getType() == IResource.FILE);
	info.incrementContentId();
	workspace.updateModificationStamp(info);
}
public boolean resourcesChanged() {
	return resourceChanged;
}
/**
 * deletion or creation -- Returns true if existence was not in sync.
 */
protected int synchronizeExistence(UnifiedTreeNode node, Resource target, int level) throws CoreException {
	boolean existsInWorkspace = node.existsInWorkspace();
	if (!existsInWorkspace && !CoreFileSystemLibrary.isCaseSensitive() && level == 0) {
		// do we have any alphabetic variants on the workspace?
		IResource variant = target.findExistingResourceVariant(target.getFullPath());
		if (variant != null)
			return RL_UNKNOWN;
	}
	
	if (existsInWorkspace) {
		if (!node.existsInFileSystem()) {
			if (target.isLocal(IResource.DEPTH_ZERO)) {
				deleteResource(node, target);
				resourceChanged = true;
				return RL_NOT_IN_SYNC;
			} else
				return RL_IN_SYNC;
		}
	} else {
		if (node.existsInFileSystem()) {
			if (!CoreFileSystemLibrary.isCaseSensitive()) {
				Container parent = (Container) target.getParent();
				if (!parent.exists()) {
					parent.getLocalManager().refresh(parent, IResource.DEPTH_ZERO, null);
					if (!parent.exists())
						return RL_NOT_IN_SYNC;
				}
				IPath location = node.getLocalLocation();
				if (!target.getName().equals(node.getLocalName()))
					return RL_IN_SYNC;
			}
			createResource(node, target);
			resourceChanged = true;
			return RL_NOT_IN_SYNC;
		}
	}
	return RL_UNKNOWN;
}
/**
 * gender change -- Returns true if gender was not in sync.
 */
protected boolean synchronizeGender(UnifiedTreeNode node, Resource target) throws CoreException {
	if (target.getType() == IResource.FILE) {
		if (!node.isFile()) {
			fileToFolder(node, target);
			resourceChanged = true;
			return false;
		}
	} else {
		if (!node.isFolder()) {
			folderToFile(node, target);
			resourceChanged = true;
			return false;
		}
	}
	return true;
}
/**
 * lastModified
 */
protected void synchronizeLastModified(UnifiedTreeNode node, Resource target) throws CoreException {
	if (target.isLocal(IResource.DEPTH_ZERO))
		resourceChanged(target, node.getLastModified());
	else
		contentAdded(target, node.getLastModified());
	resourceChanged = true;
}
public boolean visit(UnifiedTreeNode node) throws CoreException {
	Policy.checkCanceled(monitor);
	try {
		Resource target = (Resource) node.getResource();
		if (target.getType() == IResource.PROJECT)
			return true;
		if (node.existsInWorkspace() && node.existsInFileSystem()) {
			/* we don't care about folder last modified */
			if (node.isFolder() && target.getType() == IResource.FOLDER)
				return true;
			/* compare file last modified */
			long lastModifed = target.getResourceInfo(false, false).getLocalSyncInfo();
			if (lastModifed == node.getLastModified())
				return true;
		} else {
			int state = synchronizeExistence(node, target, node.getLevel());
			if (state == RL_IN_SYNC || state == RL_NOT_IN_SYNC)
				return true;
		}
		if (!synchronizeGender(node, target))
			return true;
		synchronizeLastModified(node, target);
		return true;
	} finally {
		monitor.worked(1);
	}
}
}