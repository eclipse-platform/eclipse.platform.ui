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
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.ITargetRunnable;
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
	public void get(final IResource[] resources, IProgressMonitor progress) throws TeamException {
		run(new ITargetRunnable() {
			public void run(IProgressMonitor monitor) throws TeamException {
				for (int i = 0; i < resources.length; i++) {
					getState(resources[i]).get(IResource.DEPTH_INFINITE, monitor);
				}
			}
		}, Policy.monitorFor(progress));
	}
	
	/**
	 * Get the resource from the provider to the workspace, and remember the fetched
	 * state as the base state of the resource.
	 * 
	 * @see TargetProvider.get(IResource, IRemoteTargetResource, IProgressMonitor)
	 */
	public void get(final IResource resource, final IRemoteTargetResource remote, IProgressMonitor progress) throws TeamException {
		run(new ITargetRunnable() {
			public void run(IProgressMonitor monitor) throws TeamException {
				getState(resource, remote).get(IResource.DEPTH_INFINITE, monitor);
			}
		}, Policy.monitorFor(progress));
	}


	/**
	 * Put the resources to the remote.
	 * 
	 * @see TargetProvider.put(IResource[], IProgressMonitor)
	 */
	public void put(final IResource[] resources, IProgressMonitor progress) throws TeamException {
		run(new ITargetRunnable() {
			public void run(IProgressMonitor monitor) throws TeamException {
				for (int i = 0; i < resources.length; i++) {
					getState(resources[i]).put(monitor);
				}
			}
		}, Policy.monitorFor(progress));
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
	 * @see TargetProvider#deregister(IProject)
	 */
	public void deregister(IProject project) {
		try {
			newState(project).removeState();
		} catch (TeamException e) {
			TeamPlugin.log(e.getStatus());
		}
	}
}