/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.core.target;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;

public abstract class SynchronizedTargetProvider extends TargetProvider {

	private static final int CONFIG_FORMAT_VERSION = 2;

	// The location where the target reads/writes against
	protected Site site;
	// The path relative to the site where the target reads/writes against
	protected IPath intrasitePath;
	// The URL which combines the site and relative path
	protected URL targetURL;
	
	/*
	 * These interfaces are to operations that can be performed on the array of resources,
	 * and on all resources identified by the depth parameter.
	 * @see execute(IOperation, IResource[], int, IProgressMonitor)
	 */
	protected static interface IOperation {
	}
	protected static interface IIterativeOperation extends IOperation {
		public void visit(IResource resource, int depth, IProgressMonitor progress) throws TeamException;
	}
	protected static interface IRecursiveOperation extends IOperation {
		public void visit(IResource resource, IProgressMonitor progress) throws TeamException;
	}

	/*
	 * Answers the synchronizer.
	 */		
	final protected static ISynchronizer getSynchronizer() {
		return ResourcesPlugin.getWorkspace().getSynchronizer();
	}

	public SynchronizedTargetProvider(Site site, IPath intrasitePath)  throws TeamException {
		this.intrasitePath = intrasitePath;
		this.site = site;
		// Create the combined URL here so we know it's good
		String root = getSite().getURL().toExternalForm();
		try {
			targetURL = UrlUtil.concat(root, intrasitePath);
		} catch (MalformedURLException e) {
			throw new TeamException(Policy.bind("SynchronizedTargetProvider.invalid_url_combination", root, intrasitePath.toString()), e);
		}
	}
	
	/*
	 * Answers a new state based on an existing local resource.
	 */
	abstract public ResourceState newState(IResource resource);
	
	/*
	 * Answers a new state based on an existing local resource and
	 * an associated existing remote resource.
	 */
	abstract public ResourceState newState(IResource resource, IRemoteTargetResource remote);
	
	/**
	 * @see TargetProvider#getSite()
	 */
	public Site getSite() {
		return site;
	}

	/**
	 * @see TargetProvider#getURL()
	 */
	public URL getURL() {
		return targetURL;
	}
		
	/*
	 * Get the state descriptor for a given resource.
	 */
	public ResourceState getState(IResource resource) throws TeamException {
		// Create a new resource state with default values.
		ResourceState state = newState(resource);
		state.loadState();
		return state;
	}
	
	/*
	 * Get the state descriptor for a given resource.
	 */
	public ResourceState getState(IResource resource, IRemoteTargetResource remote) throws TeamException {
		// Create a new resource state with default values.
		ResourceState state = newState(resource, remote);
		state.loadState();
		return state;
	}

	/**
	 * Get the resource from the provider to the workspace, and remember the fetched
	 * state as the base state of the resource.
	 * 
	 * @see TargetProvider.get(IResource[], int, IProgressMonitor)
	 */
	public void get(IResource[] resources, IProgressMonitor progress) throws TeamException {
		execute(new IIterativeOperation() {
			public void visit(IResource resource, int depth, IProgressMonitor progress) throws TeamException {
				getState(resource).get(depth, progress);
			}
		}, resources, IResource.DEPTH_INFINITE, progress);
	}
	
	/**
	 * Get the resource from the provider to the workspace, and remember the fetched
	 * state as the base state of the resource.
	 * 
	 * @see TargetProvider.get(IResource, IRemoteTargetResource, IProgressMonitor)
	 */
	public void get(IResource resource, final IRemoteTargetResource remote, IProgressMonitor progress) throws TeamException {
		execute(new IIterativeOperation() {
			public void visit(IResource resource, int depth, IProgressMonitor progress) throws TeamException {
				getState(resource, remote).get(depth, progress);
			}
		}, new IResource[] {resource}, IResource.DEPTH_INFINITE, progress);
	}


	/**
	 * Put the resources to the remote.
	 * 
	 * @see TargetProvider.put(IResource[], IProgressMonitor)
	 */
	public void put(IResource[] resources, IProgressMonitor progress) throws TeamException {
		execute(new IIterativeOperation() {
			public void visit(IResource resource, int depth, IProgressMonitor progress) throws TeamException {
				// The resource state must be checked-out.
				getState(resource).put(progress);
			}
		}, resources, IResource.DEPTH_INFINITE, progress);
	}

	/*
	 * Delete the corresponding remote resource.
	 * Note that deletes are always deep.
	 */
	public void delete(IResource[] resources, IProgressMonitor progress) throws TeamException {
		execute(new IIterativeOperation() {
			public void visit(IResource resource, int depth, IProgressMonitor progress) throws TeamException {
				getState(resource).delete(progress);
			}
		}, resources, IResource.DEPTH_INFINITE, progress);
	}

	/**
	 * Answer if the local resource currently has a different timestamp to the
	 * base timestamp for this resource.
	 * 
	 * @param resource the resource to test.
	 * @return <code>true</code> if the resource has a different modification
	 * timestamp, and <code>false</code> otherwise.
	 * 
	 * @see TargetProvider#isDirty(IResource)
	 */
	public boolean isDirty(IResource resource) {
		try {
			return getState(resource).isDirty();
		} catch (TeamException e) {
			TeamPlugin.log(e.getStatus());
			return true;
		}
	}

	/**
	 * Answers true if the base identifier of the given resource is different to the
	 * current released state of the resource.
	 * 
	 * @param resource the resource state to test.
	 * @return <code>true</code> if the resource base identifier is different to the
	 * current released state of the resource, and <code>false</code> otherwise.
	 * 
	 * @see TargetProvider#isOutOfDate(IResource, IProgressMonitor)
	 */
	public boolean isOutOfDate(IResource resource, IProgressMonitor monitor) throws TeamException {
		ResourceState state = getState(resource);
		return state.isOutOfDate(monitor);
	}

	/**
	 * Answer whether the resource has a corresponding remote resource in the provider.
	 * 
	 * @param resource the resource state to test.
	 * @return <code>true</code> if the resource has a corresponding remote resource,
	 * and <code>false</code> otherwise.
	 * 
	 * @see TargetProvider#hasRemote(IResource, IProgressMonitor)
	 */
	public boolean hasRemote(IResource resource, IProgressMonitor monitor) throws TeamException {
		ResourceState state = getState(resource);
		return state.hasRemote(monitor);
	}

	/**
	 * Perform the given operation on the array of resources, each to the
	 * specified depth.  Throw an exception if a problem ocurs, otherwise
	 * remain silent.
	 */
	protected void execute(
		IOperation operation,
		IResource[] resources,
		int depth,
		IProgressMonitor progress)
		throws TeamException {
			
		// For each resource in the local resources array.
		for (int i = 0; i < resources.length; i++) {
			if (operation instanceof IRecursiveOperation)
				execute((IRecursiveOperation)operation, resources[i], depth, progress);
			else
				((IIterativeOperation)operation).visit(resources[i], depth, progress);
		}

		// Cause all the resource changes to be broadcast to listeners.
//		TeamPlugin.getManager().broadcastResourceStateChanges(resources);
	}

	/**
	 * Perform the given operation on a resource to the given depth.
	 */
	protected void execute(
		IRecursiveOperation operation,
		IResource resource,
		int depth,
		IProgressMonitor progress) throws TeamException {

		operation.visit(resource, progress);

		// If the resource is a file then the depth parameter is irrelevant.
		if (resource.getType() == IResource.FILE)
			return;

		// If we are not considering any members of the container then we are done.
		if (depth == IResource.DEPTH_ZERO)
			return;

		// If the container has no children then we are done.
		IResource[] members = null;
		try {
			members = getMembers(resource);
		} catch (CoreException e) {
			if (e.getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) return;
			throw TeamPlugin.wrapException(e);
		}
		if (members.length == 0)
			return;
						
		// The next level will be one less than the current level...
		int childDepth =
			(depth == IResource.DEPTH_ONE)
				? IResource.DEPTH_ZERO
				: IResource.DEPTH_INFINITE;
				
		// Collect the responses in the multistatus.
		for (int i = 0; i < members.length; i++) {
			execute(operation, members[i], childDepth, progress);
		}
	}

	/**
	 * Answers an array of local resource members for the given resource
	 * or an empty array if the resource has no members.
	 * 
	 * @param resource the local resource whose members are required.
	 * @return an array of <code>IResource</code> or an empty array if
	 * the resource has no members.
	 */
	protected IResource[] getMembers(IResource resource) throws CoreException {
		if (resource.getType() != IResource.FILE) {
			return ((IContainer) resource).members();
		} else {
			return new IResource[0];
		}
	}
	
	/**
	 * @see TargetProvider#deregister(IProject)
	 */
	public void deregister(IProject project) {
		try {
			newState(project).removeState();
		} catch (TeamException e) {
			TeamPlugin.log(e.getStatus());
		}
	}

	/**
	 * @see TargetProvider#getIntrasitePath()
	 */
	public IPath getIntrasitePath() {
		return this.intrasitePath;
	}
	

}