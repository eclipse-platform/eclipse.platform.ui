package org.eclipse.core.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
import java.util.ArrayList;
import java.util.List;
//
public class RefreshLocalWithStatusVisitor extends RefreshLocalVisitor {
	protected MultiStatus status;
	protected List affectedResources;
public RefreshLocalWithStatusVisitor(String multiStatusTitle, IProgressMonitor monitor) {
	super(monitor);
	status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INFO, multiStatusTitle, null);
	affectedResources = new ArrayList(20);
}
protected void changed(Resource target) {
	String message = Policy.bind("localstore.resourceWasOutOfSync", target.getFullPath().toString());
	status.add(new ResourceStatus(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message));
	affectedResources.add(target);
}
public List getAffectedResources() {
	return affectedResources;
}
public MultiStatus getStatus() {
	return status;
}
protected int synchronizeExistence(UnifiedTreeNode node, Resource target, int level) throws CoreException {
	if (node.existsInWorkspace()) {
		if (!node.existsInFileSystem()) {
			if (target.isLocal(IResource.DEPTH_ZERO)) {
				changed(target);
				resourceChanged = true;
				return RL_NOT_IN_SYNC;
			} else
				return RL_IN_SYNC;
		}
	} else {
		if (node.existsInFileSystem()) {
			changed(target);
			resourceChanged = true;
			return RL_NOT_IN_SYNC;
		}
	}
	return RL_UNKNOWN;
}

protected boolean synchronizeGender(UnifiedTreeNode node, Resource target) throws CoreException {
	if (target.getType() == IResource.FILE) {
		if (!node.isFile()) {
			changed(target);
			resourceChanged = true;
			return false;
		}
	} else {
		if (!node.isFolder()) {
			changed(target);
			resourceChanged = true;
			return false;
		}
	}
	return true;
}
protected boolean synchronizeLastModified(UnifiedTreeNode node, Resource target) throws CoreException {
	changed(target);
	resourceChanged = true;
	return false;
}
}
