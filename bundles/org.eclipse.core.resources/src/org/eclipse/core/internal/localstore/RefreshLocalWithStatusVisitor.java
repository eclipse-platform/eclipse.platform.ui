package org.eclipse.core.internal.localstore;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import java.util.ArrayList;
import java.util.List;
//
public class RefreshLocalWithStatusVisitor extends RefreshLocalVisitor {
	protected MultiStatus status;
	protected String message;
	protected List affectedResources;
public RefreshLocalWithStatusVisitor(String multiStatusTitle, String eachResourceMessage, IProgressMonitor monitor) {
	super(monitor);
	status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, multiStatusTitle, null);
	affectedResources = new ArrayList(20);
	message = eachResourceMessage;
}
protected void changed(Resource target) {
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
