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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.*;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.target.*;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.TeamPlugin;

public abstract class SynchronizedTargetProvider extends TargetProvider {

	private static final int CONFIG_FORMAT_VERSION = 2;

	private final int depth = IResource.DEPTH_INFINITE;

	/**
	 * These interfaces are to operations that can be performed on the array of resources,
	 * and on all resources identified by the depth parameter.
	 * @see execute(IOperation, IResource[], int, IProgressMonitor)
	 */
	protected static interface IOperation {
	}
	protected static interface IIterativeOperation extends IOperation {
		public IStatus visit(IResource resource, int depth, IProgressMonitor progress);
	}
	protected static interface IRecursiveOperation extends IOperation {
		public IStatus visit(IResource resource, IProgressMonitor progress);
	}

	/**
	 * Answers the synchronizer.
	 */		
	final protected static ISynchronizer getSynchronizer() {
		return ResourcesPlugin.getWorkspace().getSynchronizer();
	}

	/**
	 * Answers a new state based on an existing local resource.
	 */
	abstract public ResourceState newState(IResource resource);
	
	/**
	 * Answers a new state based on an existing local resource and
	 * an associated existing remote resource.
	 */
	abstract public ResourceState newState(IResource resource, IRemoteTargetResource remote);
	
	/**
	 * Get the state descriptor for a given resource.
	 */
	public ResourceState getState(IResource resource) {
		// Create a new resource state with default values.
		ResourceState state = newState(resource);
		state.loadState();
		return state;
	}
	
	/**
	 * Get the state descriptor for a given resource.
	 */
	public ResourceState getState(IResource resource, IRemoteTargetResource remote) {
		// Create a new resource state with default values.
		ResourceState state = newState(resource, remote);
		state.loadState();
		return state;
	}

	/**
	 * Get the resource from the provider to the workspace, and remember the fetched
	 * state as the base state of the resource.
	 * 
	 * @see ITeamProvider.get(IResource[], int, IProgressMonitor)
	 */
	public void get(IResource[] resources, IProgressMonitor progress)
		throws TeamException {
		execute(new IIterativeOperation() {
			public IStatus visit(IResource resource, int depth, IProgressMonitor progress) {
				ResourceState state = getState(resource);
				return new Symmetria().get(state, depth, progress);
			}
		}, resources, depth, progress);
	}
	
	/**
	 * Get the resource from the provider to the workspace, and remember the fetched
	 * state as the base state of the resource.
	 * 
	 * @see ITeamProvider.get(IResource[], int, IProgressMonitor)
	 */
	public void get(IResource resource, final IRemoteTargetResource remote, IProgressMonitor progress)
		throws TeamException {
		execute(new IIterativeOperation() {
			public IStatus visit(IResource resource, int depth, IProgressMonitor progress) {
				ResourceState state = getState(resource, remote);
				return new Symmetria().get(state, IResource.DEPTH_INFINITE, progress);
			}
		}, new IResource[] {resource}, IResource.DEPTH_ZERO, progress);
	}


	/**
	 * Put the resources to the remote.
	 */
	public void put(IResource[] resources, IProgressMonitor progress)
		throws TeamException {
		execute(new IRecursiveOperation() {
			public IStatus visit(IResource resource, IProgressMonitor progress) {
				// The resource state must be checked-out.
				ResourceState state = getState(resource);
				return state.checkin(progress);
			}
		}, resources, depth, progress);
	}

	/**
	 * Delete the corresponding remote resource.
	 * Note that deletes are always deep.
	 */
	public void delete(IResource[] resources, IProgressMonitor progress)
		throws TeamException {
		execute(new IIterativeOperation() {
			public IStatus visit(IResource resource, int depth, IProgressMonitor progress) {
				ResourceState state = getState(resource);
				return state.delete(progress);
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
	 * @see ITeamSynch#isDirty(IResource)
	 */
	public boolean isDirty(IResource resource) {
		ResourceState state = getState(resource);
		return state.isDirty(resource);
	}

	/**
	 * Answers true if the base identifier of the given resource is different to the
	 * current released state of the resource.
	 * 
	 * @param resource the resource state to test.
	 * @return <code>true</code> if the resource base identifier is different to the
	 * current released state of the resource, and <code>false</code> otherwise.
	 * @see ITeamSynch#isOutOfDate(IResource)
	 */
	public boolean isOutOfDate(IResource resource) {
		ResourceState state = getState(resource);
		return state.isOutOfDate();
	}

	/**
	 * Answer whether the resource has a corresponding remote resource in the provider.
	 * 
	 * @param resource the resource state to test.
	 * @return <code>true</code> if the resource has a corresponding remote resource,
	 * and <code>false</code> otherwise.
	 * @see ITeamSynch#hasRemote(IResource)
	 */
	public boolean hasRemote(IResource resource) {
		ResourceState state = getState(resource);
		return state.hasRemote();
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
			
		// Create an array to hold the status for each resource.
		IStatus[] statuses = new IStatus[resources.length];
		
		// Remember if a failure occurred in any resource, so we can throw an exception at the end.
		boolean failureOccurred = false;

		// For each resource in the local resources array.
		for (int i = 0; i < resources.length; i++) {
			if (operation instanceof IRecursiveOperation)
				statuses[i] = execute((IRecursiveOperation)operation, resources[i], depth, progress);
			else
				statuses[i] = ((IIterativeOperation)operation).visit(resources[i], depth, progress);
			failureOccurred = failureOccurred || (!statuses[i].isOK());
		}

		// Finally, if any problems occurred, throw the exeption with all the statuses,
		// but if there were no problems exit silently.
		if (failureOccurred)
			throw new TeamException(
				new MultiStatus(
					TeamPlugin.ID,
					0,	// code - we don't have one
					statuses,
					Policy.bind("multiStatus.errorsOccurred"), //$NON-NLS-1$
					null));

		// Cause all the resource changes to be broadcast to listeners.
//		TeamPlugin.getManager().broadcastResourceStateChanges(resources);
	}

	/**
	 * Perform the given operation on a resource to the given depth.
	 */
	protected IStatus execute(
		IRecursiveOperation operation,
		IResource resource,
		int depth,
		IProgressMonitor progress) {

		// Visit the given resource first.
		IStatus status = operation.visit(resource, progress);

		// If the resource is a file then the depth parameter is irrelevant.
		if (resource.getType() == IResource.FILE)
			return status;

		// If we are not considering any members of the container then we are done.
		if (depth == IResource.DEPTH_ZERO)
			return status;

		// If the operation was unsuccessful, do not attempt to go deep.
		if (!status.isOK())
			return status;

		// If the container has no children then we are done.
		IResource[] members = getMembers(resource);
		if (members.length == 0)
			return status;
		
		// There are children and we are going deep, the response will be a multi-status.
		MultiStatus multiStatus =
			new MultiStatus(
				status.getPlugin(),
				status.getCode(),
				status.getMessage(),
				status.getException());
				
		// The next level will be one less than the current level...
		int childDepth =
			(depth == IResource.DEPTH_ONE)
				? IResource.DEPTH_ZERO
				: IResource.DEPTH_INFINITE;
				
		// Collect the responses in the multistatus.
		for (int i = 0; i < members.length; i++)
			multiStatus.add(execute(operation, members[i], childDepth, progress));

		return multiStatus;
	}

	/**
	 * Answers an array of local resource members for the given resource
	 * or an empty arrray if the resource has no members.
	 * 
	 * @param resource the local resource whose members are required.
	 * @return an array of <code>IResource</code> or an empty array if
	 * the resource has no members.
	 */
	protected IResource[] getMembers(IResource resource) {
		if (resource.getType() != IResource.FILE) {
			try {
				return ((IContainer) resource).members();
			} catch (CoreException exception) {
				exception.printStackTrace();
				throw new RuntimeException();
			}
		} //end-if
		else
			return new IResource[0];
	}	
}