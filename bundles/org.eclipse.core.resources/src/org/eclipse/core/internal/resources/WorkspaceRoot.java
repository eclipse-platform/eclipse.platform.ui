package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.HashMap;

import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class WorkspaceRoot extends Container implements IWorkspaceRoot {
	/**
	 * As an optimization, we store a table of project handles
	 * that have been requested from this root.  This maps project
	 * name strings to project handles.
	 */
	private HashMap projectTable = new HashMap(10);
	
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
	int updateFlags = force ? IResource.FORCE : IResource.NONE;
	delete(updateFlags, monitor);
}
/**
 * @see IWorkspaceRoot#delete
 */
public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {
	int updateFlags = force ? IResource.FORCE : IResource.NONE;
	updateFlags |= deleteContent ? IResource.ALWAYS_DELETE_PROJECT_CONTENT : IResource.NEVER_DELETE_PROJECT_CONTENT;
	delete(updateFlags, monitor);
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
	//first check our project cache
	Project result = (Project)projectTable.get(name);
	if (result == null) {
		IPath path = Path.ROOT.append(name);
		String message = "Path for project must have only one segment.";
		Assert.isLegal(path.segmentCount() == ICoreConstants.PROJECT_SEGMENT_LENGTH, message);
		result = new Project(path, workspace);
		projectTable.put(name, result);
	}
	return result;
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
