/*******************************************************************************
 *  Copyright (c) 2004, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM - Initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427, 483862
 *******************************************************************************/
package org.eclipse.core.internal.refresh;

import java.util.*;
import org.eclipse.core.internal.localstore.PrefixPool;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * The <code>RefreshJob</code> class maintains a list of resources that
 * need to be refreshed, and periodically schedules itself to perform the
 * refreshes in the background.
 *
 * @since 3.0
 */
public class RefreshJob extends WorkspaceJob {
	private static final long UPDATE_DELAY = 200;
	/**
	 * List of refresh requests. Requests are processed in order from
	 * the end of the list. Requests can be added to either the beginning
	 * or the end of the list depending on whether they are explicit user
	 * requests or background refresh requests.
	 */
	private final List<IResource> fRequests;

	/**
	 * The history of path prefixes visited during this refresh job invocation.
	 * This is used to prevent infinite refresh loops caused by symbolic links in the file system.
	 */
	private PrefixPool pathPrefixHistory, rootPathHistory;

	public RefreshJob() {
		super(Messages.refresh_jobName);
		fRequests = new ArrayList<>(1);
	}

	/**
	 * Adds the given resource to the set of resources that need refreshing.
	 * Synchronized in order to protect the collection during add.
	 * @param resource
	 */
	private synchronized void addRequest(IResource resource) {
		IPath toAdd = resource.getFullPath();
		for (Iterator<IResource> it = fRequests.iterator(); it.hasNext();) {
			IPath request = it.next().getFullPath();
			//discard any existing requests the same or below the resource to be added
			if (toAdd.isPrefixOf(request))
				it.remove();
			//nothing to do if the resource to be added is a child of an existing request
			else if (request.isPrefixOf(toAdd))
				return;
		}
		//finally add the new request to the front of the queue
		fRequests.add(resource);
	}

	private synchronized void addRequests(List<IResource> list) {
		//add requests to the end of the queue
		fRequests.addAll(0, list);
	}

	@Override
	public boolean belongsTo(Object family) {
		return family == ResourcesPlugin.FAMILY_AUTO_REFRESH;
	}

	/**
	 * This method adds all members at the specified depth from the resource
	 * to the provided list.
	 */
	private List<IResource> collectChildrenToDepth(IResource resource, ArrayList<IResource> children, int depth) {
		if (resource.getType() == IResource.FILE)
			return children;
		IResource[] members;
		try {
			members = ((IContainer) resource).members();
		} catch (CoreException e) {
			//resource is not accessible - just return what we have
			return children;
		}
		for (int i = 0; i < members.length; i++) {
			if (members[i].getType() == IResource.FILE)
				continue;
			if (depth <= 1)
				children.add(members[i]);
			else
				collectChildrenToDepth(members[i], children, depth - 1);
		}
		return children;
	}

	/**
	 * Returns the path prefixes visited by this job so far.
	 */
	public PrefixPool getPathPrefixHistory() {
		if (pathPrefixHistory == null)
			pathPrefixHistory = new PrefixPool(20);
		return pathPrefixHistory;
	}

	/**
	 * Returns the root paths visited by this job so far.
	 */
	public PrefixPool getRootPathHistory() {
		if (rootPathHistory == null)
			rootPathHistory = new PrefixPool(20);
		return rootPathHistory;
	}

	/**
	 * Returns the next item to refresh, or <code>null</code> if there are no requests
	 */
	private synchronized IResource nextRequest() {
		// synchronized: in order to atomically obtain and clear requests
		int len = fRequests.size();
		if (len == 0)
			return null;
		return fRequests.remove(len - 1);
	}

	/**
	 * @see org.eclipse.core.resources.refresh.IRefreshResult#refresh
	 */
	public void refresh(IResource resource) {
		if (resource == null)
			return;
		addRequest(resource);
		schedule(UPDATE_DELAY);
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		String msg = Messages.refresh_refreshErr;
		MultiStatus errors = new MultiStatus(ResourcesPlugin.PI_RESOURCES, 1, msg, null);
		long longestRefresh = 0;
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		try {
			if (Policy.DEBUG_AUTO_REFRESH)
				Policy.debug(RefreshManager.DEBUG_PREFIX + " starting refresh job"); //$NON-NLS-1$
			int refreshCount = 0;
			int depth = 2;

			IResource toRefresh;
			while ((toRefresh = nextRequest()) != null) {
				try {
					subMonitor.setWorkRemaining(Math.max(fRequests.size(), 100));
					refreshCount++;
					long refreshTime = -System.currentTimeMillis();
					toRefresh.refreshLocal(1000 + depth, subMonitor.split(1));
					refreshTime += System.currentTimeMillis();
					if (refreshTime > longestRefresh)
						longestRefresh = refreshTime;
					//show occasional progress
					if (refreshCount % 1000 == 0) {
						//be polite to other threads (no effect on some platforms)
						Thread.yield();
						//throttle depth if it takes too long
						if (longestRefresh > 2000 && depth > 1) {
							depth = 1;
						}
						if (longestRefresh < 1000) {
							depth *= 2;
						}
						longestRefresh = 0;
					}
					addRequests(collectChildrenToDepth(toRefresh, new ArrayList<IResource>(), depth));
				} catch (CoreException e) {
					errors.merge(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, 1, errors.getMessage(), e));
				}
			}
		} finally {
			pathPrefixHistory = null;
			rootPathHistory = null;
			if (Policy.DEBUG_AUTO_REFRESH)
				Policy.debug(RefreshManager.DEBUG_PREFIX + " finished refresh job in: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (!errors.isOK())
			return errors;
		return Status.OK_STATUS;
	}

	@Override
	public synchronized boolean shouldRun() {
		return !fRequests.isEmpty();
	}

	/**
	 * Starts the refresh job
	 */
	public void start() {
		if (Policy.DEBUG_AUTO_REFRESH)
			Policy.debug(RefreshManager.DEBUG_PREFIX + " enabling auto-refresh"); //$NON-NLS-1$
	}

	/**
	 * Stops the refresh job
	 */
	public void stop() {
		if (Policy.DEBUG_AUTO_REFRESH)
			Policy.debug(RefreshManager.DEBUG_PREFIX + " disabling auto-refresh"); //$NON-NLS-1$
		cancel();
	}
}
