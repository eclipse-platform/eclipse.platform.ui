/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.resources;


import java.net.URI;
import java.util.*;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

public class WorkspaceRoot extends Container implements IWorkspaceRoot {
	/**
	 * As an optimization, we store a table of project handles
	 * that have been requested from this root.  This maps project
	 * name strings to project handles.
	 */
	private final Map<String, Project> projectTable = Collections.synchronizedMap(new HashMap<String, Project>(16));
	
	/**
	 * Cache of the canonicalized platform location.
	 */
	private final IPath workspaceLocation;

	protected WorkspaceRoot(IPath path, Workspace container) {
		super(path, container);
		Assert.isTrue(path.equals(Path.ROOT));
		workspaceLocation = FileUtil.canonicalPath(Platform.getLocation());
		Assert.isNotNull(workspaceLocation);
	}

	/**
	 * @see IWorkspaceRoot#delete(boolean, boolean, IProgressMonitor)
	 */
	public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException {
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		updateFlags |= deleteContent ? IResource.ALWAYS_DELETE_PROJECT_CONTENT : IResource.NEVER_DELETE_PROJECT_CONTENT;
		delete(updateFlags, monitor);
	}

	/**
	 * @see org.eclipse.core.internal.resources.Resource#delete(boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void delete(boolean force, IProgressMonitor monitor) throws CoreException {
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		delete(updateFlags, monitor);
	}

	/**
	 * @see org.eclipse.core.internal.resources.Resource#exists(int, boolean)
	 */
	public boolean exists(int flags, boolean checkType) {
		return true;
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceRoot#findContainersForLocation(org.eclipse.core.runtime.IPath)
	 * @deprecated
	 */
	public IContainer[] findContainersForLocation(IPath location) {
		return findContainersForLocationURI(URIUtil.toURI(location.makeAbsolute()));
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceRoot#findContainersForLocationURI(java.net.URI)
	 */
	public IContainer[] findContainersForLocationURI(URI location) {
		return findContainersForLocationURI(location, NONE);
	}
	
	/**
	 * @see org.eclipse.core.resources.IWorkspaceRoot#findContainersForLocationURI(java.net.URI, int)
	 */
	public IContainer[] findContainersForLocationURI(URI location, int memberFlags) {
		if (!location.isAbsolute())
			throw new IllegalArgumentException();
		return (IContainer[]) getLocalManager().allResourcesFor(location, false, memberFlags);
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceRoot#findFilesForLocation(org.eclipse.core.runtime.IPath)
	 * @deprecated
	 */
	public IFile[] findFilesForLocation(IPath location) {
		return findFilesForLocationURI(URIUtil.toURI(location.makeAbsolute()));
	}

	/**
	 * @see org.eclipse.core.resources.IWorkspaceRoot#findFilesForLocationURI(java.net.URI)
	 */
	public IFile[] findFilesForLocationURI(URI location) {
		return findFilesForLocationURI(location, NONE);
	}
	
	/**
	 * @see org.eclipse.core.resources.IWorkspaceRoot#findFilesForLocationURI(java.net.URI, int)
	 */
	public IFile[] findFilesForLocationURI(URI location, int memberFlags) {
		if (!location.isAbsolute())
			throw new IllegalArgumentException();
		return (IFile[]) getLocalManager().allResourcesFor(location, true, memberFlags);
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
		String enc = ResourcesPlugin.getPlugin().getPluginPreferences().getString(ResourcesPlugin.PREF_ENCODING);
		return enc == null || enc.length() == 0 ? null : enc;
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
		return workspaceLocation;
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
		Project result = projectTable.get(name);
		if (result == null) {
			IPath projectPath = new Path(null, name).makeAbsolute();
			String message = "Path for project must have only one segment."; //$NON-NLS-1$
			Assert.isLegal(projectPath.segmentCount() == ICoreConstants.PROJECT_SEGMENT_LENGTH, message);
			//try to get the project using a canonical name
			String canonicalName = projectPath.lastSegment();
			result = projectTable.get(canonicalName);
			if (result != null)
				return result;
			result = new Project(projectPath, workspace);
			projectTable.put(canonicalName, result);
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
		return getProjects(IResource.NONE);
	}

	/**
	 * @see IWorkspaceRoot#getProjects(int)
	 */
	public IProject[] getProjects(int memberFlags) {
		IResource[] roots = getChildren(memberFlags);
		IProject[] result = new IProject[roots.length];
		try {
			System.arraycopy(roots, 0, result, 0, roots.length);
		} catch (ArrayStoreException ex) {
			// Shouldn't happen since only projects should be children of the workspace root
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].getType() != IResource.PROJECT)
					Policy.log(IStatus.ERROR, NLS.bind("{0} is an invalid child of the workspace root.", //$NON-NLS-1$
							roots[i]), null);

			}
			throw ex;
		}
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
		IResource[] children = getChildren(IResource.NONE);
		for (int i = 0; i < children.length; i++)
			((Resource) children[i]).internalSetLocal(flag, depth);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.resources.Resource#isDerived(int)
	 */
	public boolean isDerived(int options) {
		return false;//the root is never derived
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.resources.Resource#isHidden()
	 */
	public boolean isHidden() {
		return false;//the root is never hidden
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.resources.Resource#isHidden(int)
	 */
	public boolean isHidden(int options) {
		return false;//the root is never hidden
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.resources.Resource#isTeamPrivateMember(int)
	 */
	public boolean isTeamPrivateMember(int options) {
		return false;//the root is never a team private member
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.resources.Resource#isLinked(int)
	 */
	public boolean isLinked(int options) {
		return false;//the root is never linked
	}
	
	/**
	 * @see IResource#isLocal(int)
	 * @deprecated
	 */
	public boolean isLocal(int depth) {
		// the flags parameter is ignored for the workspace root so pass anything
		return isLocal(-1, depth);
	}

	/**
	 * @see IResource#isLocal(int)
	 * @deprecated
	 */
	public boolean isLocal(int flags, int depth) {
		// don't check the flags....workspace root is always local
		if (depth == DEPTH_ZERO)
			return true;
		if (depth == DEPTH_ONE)
			depth = DEPTH_ZERO;
		// get the children via the workspace since we know that this
		// resource exists (it is local).
		IResource[] children = getChildren(IResource.NONE);
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
	public void setDefaultCharset(String charset) {
		// directly change the Resource plugin's preference for encoding
		Preferences resourcesPreferences = ResourcesPlugin.getPlugin().getPluginPreferences();
		if (charset != null)
			resourcesPreferences.setValue(ResourcesPlugin.PREF_ENCODING, charset);
		else
			resourcesPreferences.setToDefault(ResourcesPlugin.PREF_ENCODING);
	}
	
	public void setHidden(boolean isHidden) {
		//workspace root cannot be set hidden
	}

	/**
	 * @see IResource#setLocalTimeStamp(long)
	 */
	public long setLocalTimeStamp(long value) {
		if (value < 0)
			throw new IllegalArgumentException("Illegal time stamp: " + value); //$NON-NLS-1$
		//can't set local time for root
		return value;
	}

	/**
	 * @deprecated
	 * @see IResource#setReadOnly(boolean)
	 */
	public void setReadOnly(boolean readonly) {
		//can't set the root read only
	}

	/**
	 * @see IResource#touch(IProgressMonitor)
	 */
	public void touch(IProgressMonitor monitor) {
		// do nothing for the workspace root
	}
}
