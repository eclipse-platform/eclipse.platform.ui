/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.refresh;

import java.util.*;

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * The <code>RefreshJob</code> class maintains a list of resources that
 * need to be refreshed, and periodically schedules itself to perform the
 * refreshes in the background.
 */
public class RefreshJob extends WorkspaceJob {
	private static final long UPDATE_DELAY = 200;
	/**
	 * Flag indicating if this refresh job should be running
	 */
	private boolean active = false;
	/**
	 * List of refresh requests
	 */
	private final List fRequests;

	public RefreshJob() {
		super(Policy.bind("refresh.jobName")); //$NON-NLS-1$
		setPriority(Job.LONG);
		setSystem(true);
		setRule(ResourcesPlugin.getWorkspace().getRoot());
		fRequests = new ArrayList(1);
	}

	/**
	 * Adds the given resource to the set of resources that need refreshing.
	 * Synchronized in order to protect the collection during add.
	 * @param resource
	 */
	private synchronized void addRequest(IResource resource) {
		//discard if the resource to be added is a sibling or child of an existing request
		IPath toAdd = resource.getFullPath();
		int size = fRequests.size();
		for (int i = 0; i < size; i++)
			if (((IResource) fRequests.get(i)).getFullPath().isPrefixOf(toAdd))
				return;
		//discard any existing requests below the resource to be added
		for (Iterator it = fRequests.iterator(); it.hasNext();)
			if (toAdd.isPrefixOf(((IResource) it.next()).getFullPath()))
				it.remove();
		//finally add the new request
		fRequests.add(resource);
	}

	private synchronized IResource[] getRequests() {
		// synchronized: in order to atomically obtain and clear requests
		IResource[] toRefresh = (IResource[]) fRequests.toArray(new IResource[fRequests.size()]);
		fRequests.clear();
		return toRefresh;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.refresh.IRefreshResult#refresh
	 */
	public void refresh(IResource resource) {
		if (resource == null)
			return;
		addRequest(resource);
		if (active)
			schedule(UPDATE_DELAY);
	}

	/* (non-Javadoc)
	 * @see WorkspaceJob#runInWorkspace
	 */
	public IStatus runInWorkspace(IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		String msg = Policy.bind("refresh.refreshErr"); //$NON-NLS-1$
		MultiStatus errors = new MultiStatus(ResourcesPlugin.PI_RESOURCES, 1, msg, null);
		try {
			if (RefreshManager.DEBUG)
				System.out.println(RefreshManager.DEBUG_PREFIX + " starting refresh job"); //$NON-NLS-1$
			IResource[] toRefresh = getRequests();
			monitor.beginTask(Policy.bind("refresh.task"), toRefresh.length); //$NON-NLS-1$					
			for (int i = 0; i < toRefresh.length; i++) {
				if (monitor.isCanceled())
					throw new OperationCanceledException();
				try {
					toRefresh[i].refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 1));
				} catch (CoreException e) {
					errors.merge(e.getStatus());
				}
			}
		} finally {
			monitor.done();
			if (RefreshManager.DEBUG)
				System.out.println(RefreshManager.DEBUG_PREFIX + " finished refresh job in: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!errors.isOK())
			return errors;
		return Status.OK_STATUS;
	}

	/**
	 * Starts the refresh job
	 */
	public void start() {
		if (RefreshManager.DEBUG)
			System.out.println(RefreshManager.DEBUG_PREFIX + " enabling auto-refresh"); //$NON-NLS-1$
		active = true;
	}

	/**
	 * Stops the refresh job
	 */
	public void stop() {
		if (active && RefreshManager.DEBUG)
			System.out.println(RefreshManager.DEBUG_PREFIX + " disabling auto-refresh"); //$NON-NLS-1$
		active = false;
		cancel();
	}
}