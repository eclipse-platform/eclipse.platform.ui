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
package org.eclipse.core.internal.resources;

import java.util.HashMap;
import org.eclipse.core.internal.utils.Assert;
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
	 * @see IResource#clearHistory(IProgressMonitor)
	 */
	public void clearHistory(IProgressMonitor monitor) throws CoreException {
		getLocalManager().getHistoryStore().removeAll();
	}

	/**
	 * @see IResource#delete(boolean, IProgressMonitor)
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		delete(updateFlags, monitor);
	}

	/**
	 * @see IWorkspaceRoot#delete(boolean, boolean, IProgressMonitor)
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
	 * @see IWorkspaceRoot#findContainersForLocation(IPath)
	 */
	public IContainer[] findContainersForLocation(IPath location) {
		return (IContainer[]) getLocalManager().allResourcesFor(location, false);
	}

	/**
	 * @see IWorkspaceRoot#findFilesForLocation(IPath)
	 */
	public IFile[] findFilesForLocation(IPath location) {
		return (IFile[]) getLocalManager().allResourcesFor(location, true);
	}

	/**
	 * @see IWorkspaceRoot#getContainerForLocation(IPath)
	 */
	public IContainer getContainerForLocation(IPath location) {
		return getLocalManager().containerForLocation(location);
	}

	/**
	 * @see IContainer#getDefaultCharset(boolean)
	 */
	public String getDefaultCharset(boolean checkImplicit) {
		if (checkImplicit)
			return ResourcesPlugin.getEncoding();
		return ResourcesPlugin.getPlugin().getPluginPreferences().getString(ResourcesPlugin.PREF_ENCODING);
	}

	/**
	 * @see IWorkspaceRoot#getFileForLocation(IPath)
	 */
	public IFile getFileForLocation(IPath location) {
		return getLocalManager().fileForLocation(location);
	}

	/**
	 * @see IResource#getLocalTimeStamp()
	 */
	public long getLocalTimeStamp() {
		return IResource.NULL_STAMP;
	}

	/**
	 * @see IResource#getLocation()
	 */
	public IPath getLocation() {
		return Platform.getLocation();
	}

	/**
	 * @see IResource#getName()
	 */
	public String getName() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * @see IResource#getParent()
	 */
	public IContainer getParent() {
		return null;
	}

	/**
	 * @see IResource#getProject()
	 */
	public IProject getProject() {
		return null;
	}

	/**
	 * @see IWorkspaceRoot#getProject(String)
	 */
	public IProject getProject(String name) {
		//first check our project cache
		Project result = (Project) projectTable.get(name);
		if (result == null) {
			IPath path = Path.ROOT.append(name);
			String message = "Path for project must have only one segment."; //$NON-NLS-1$
			Assert.isLegal(path.segmentCount() == ICoreConstants.PROJECT_SEGMENT_LENGTH, message);
			result = new Project(path, workspace);
			projectTable.put(name, result);
		}
		return result;
	}

	/**
	 * @see IResource#getProjectRelativePath()
	 */
	public IPath getProjectRelativePath() {
		return Path.EMPTY;
	}

	/**
	 * @see IWorkspaceRoot#getProjects()
	 */
	public IProject[] getProjects() {
		IResource[] roots = getChildren(Path.ROOT, false);
		IProject[] result = new IProject[roots.length];
		System.arraycopy(roots, 0, result, 0, roots.length);
		return result;
	}

	/**
	 * @see IResource#getType()
	 */
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
	 * @see IResource#isLocal(int)
	 */
	public boolean isLocal(int depth) {
		// the flags parm is ignored for the workspace root so pass anything
		return isLocal(-1, depth);
	}

	/**
	 * @see IResource#isLocal(int)
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
	 * @see IResource#isPhantom()
	 */
	public boolean isPhantom() {
		return false;
	}

	/**
	 * @see IContainer#setDefaultCharset(String)
	 * @deprecated Replaced by {@link #setDefaultCharset(String, IProgressMonitor)} which 
	 * 	is a workspace operation and reports changes in resource deltas.
	 */
	public void setDefaultCharset(String charset) throws CoreException {
		// directly change the Resource plugin's preference for encoding
		Preferences resourcesPreferences = ResourcesPlugin.getPlugin().getPluginPreferences();
		if (charset != null)
			resourcesPreferences.setValue(ResourcesPlugin.PREF_ENCODING, charset);
		else
			resourcesPreferences.setToDefault(ResourcesPlugin.PREF_ENCODING);
	}

	/**
	 * @see IResource#setLocalTimeStamp(long)
	 */
	public long setLocalTimeStamp(long value) throws CoreException {
		if (value < 0)
			throw new IllegalArgumentException("Illegal time stamp: " + value); //$NON-NLS-1$
		//can't set local time for root
		return value;
	}

	/**
	 * @see IResource#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readonly) {
		//can't set the root read only
	}

	/**
	 * Returns true if this resource has the potential to be
	 * (or have been) synchronized.  
	 */
	public boolean synchronizing() {
		return false;
	}

	/**
	 * @see IResource#touch(IProgressMonitor)
	 */
	public void touch(IProgressMonitor monitor) throws CoreException {
		// do nothing for the workspace root
	}
}