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
	int state = super.synchronizeExistence(node, target, level);
	if (state == RL_NOT_IN_SYNC)
		changed(target);
	return state;
}
protected boolean synchronizeGender(UnifiedTreeNode node, Resource target) throws CoreException {
	boolean inSync = super.synchronizeGender(node, target);
	if (!inSync)
		changed(target);
	return inSync;
}
protected boolean synchronizeLastModified(UnifiedTreeNode node, Resource target) throws CoreException {
	boolean inSync = super.synchronizeLastModified(node, target);
	if (!inSync)
		changed(target);
	return inSync;
}
}
