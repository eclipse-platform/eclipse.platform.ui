package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;

public class WorkspaceRoot extends Container implements IWorkspaceRoot {
protected WorkspaceRoot(IPath path, Workspace container) {
	super(path, container);
	Assert.isTrue(path.equals(Path.ROOT));
}
/**
 * @see IResource
 */
public void clearHistory(IProgressMonitor monitor) throws CoreException {
	getLocalManager().getHistoryStore().removeAll();
}
/**
 * @see IResource#delete
 */
public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
	delete(true, force, monitor);
}
/**
 * @see IWorkspaceRoot#delete
 */
public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String title = Policy.bind("deleting", new String[] { getFullPath().toString()});
		monitor.beginTask(title, Policy.totalWork);
		try {
			workspace.prepareOperation();

			workspace.beginOperation(true);
			IProject[] projects = getProjects();
			IProgressMonitor sub = Policy.subMonitorFor(monitor, Policy.opWork);
			try {
				sub.beginTask(title, projects.length);
				for (int i = 0; i < projects.length; i++)
					projects[i].delete(deleteContent, force, Policy.subMonitorFor(sub, 1));
			} finally {
				sub.done();
			}
			// need to clear out the root info
			workspace.getMarkerManager().removeMarkers(this);
			getPropertyManager().deleteProperties(this);
			getResourceInfo(false, false).clearSessionProperties();
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
public boolean exists(int flags, boolean checkType) {
	return true;
}
/**
 * @see IWorkspaceRoot
 */
public IContainer getContainerForLocation(IPath location) {
	return getLocalManager().containerFor(location);
}
/**
 * @see IWorkspaceRoot
 */
public IFile getFileForLocation(IPath location) {
	return getLocalManager().fileFor(location);
}
/**
 * @see IResource#getLocation
 */
public IPath getLocation() {
	return Platform.getLocation();
}
/**
 * @see IResource#getName
 */
public String getName() {
	return "";
}
/**
 * @see IResource#getParent
 */
public IContainer getParent() {
	return null;
}
/**
 * @see IResource#getProject
 */
public IProject getProject() {
	return null;
}
/**
 * @see IResource#getProject
 */
public IProject getProject(String name) {
	Path path = new Path(name);
	Assert.isLegal(path.segmentCount() == ICoreConstants.PROJECT_SEGMENT_LENGTH, Policy.bind("projectPath", null));
	return new Project(Path.ROOT.append(name), workspace);
}
/**
 * @see IResource#getProjectRelativePath
 */
public IPath getProjectRelativePath() {
	return Path.EMPTY;
}
/**
 * @see IWorkspaceRoot
 */
public IProject[] getProjects() {
	IResource[] roots = getChildren(Path.ROOT, false);
	IProject[] result = new IProject[roots.length];
	System.arraycopy(roots, 0, result, 0, roots.length);
	return result;
}
public int getType() {
	return IResource.ROOT;
}
public void internalSetLocal(boolean flag, int depth) throws CoreException {
	// do nothing for the root, but call for its children
	if (depth == IResource.DEPTH_ZERO)
		return;
	if (depth == IResource.DEPTH_ONE)
		depth = IResource.DEPTH_ZERO;
	// get the children via the workspace since we know that this
	// resource exists (it is local).
	IResource[] children = getChildren(this, false);
	for (int i = 0; i < children.length; i++)
		 ((Resource) children[i]).internalSetLocal(flag, depth);
}
/**
 * @see IResource#isLocal
 */
public boolean isLocal(int depth) {
	// the flags parm is ignored for the workspace root so pass anything
	return isLocal(-1, depth);
}
/**
 * @see IResource#isLocal
 */
public boolean isLocal(int flags, int depth) {
	// don't check the flags....workspace root is always local
	if (depth == DEPTH_ZERO)
		return true;
	if (depth == DEPTH_ONE)
		depth = DEPTH_ZERO;
	// get the children via the workspace since we know that this
	// resource exists (it is local).
	IResource[] children = getChildren(this, false);
	for (int i = 0; i < children.length; i++)
		if (!children[i].isLocal(depth))
			return false;
	return true;
}
/**
 * @see IResource#isPhantom
 */
public boolean isPhantom() {
	return false;
}
/**
 * @see IResource
 */
public void setReadOnly(boolean readonly) {
}
/**
 * Returns true if this resource has the potential to be
 * (or have been) synchronized.  
 */
public boolean synchronizing() {
	return false;
}
/**
 * @see IResource#touch
 */
public void touch(IProgressMonitor monitor) throws CoreException {
	// do nothing for the workspace root
}
}
