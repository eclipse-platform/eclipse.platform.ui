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
import java.util.ArrayList;

import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.refresh.IRefreshMonitor;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;

/**
 * The <code>PollingMonitor</code> is an <code>IRefreshMonitor</code> that
 * polls the filesystem rather than registering natively for callbacks.
 */
public class PollingMonitor extends Job implements IRefreshMonitor {
	/*
	 * The roots of resources which should be polled
	 */
	private ArrayList resourceRoots;
	private RefreshManager manager;
	private long pollingDelay = 30000;
	/**
	 * Creates a new polling monitor.
	 */
	public PollingMonitor(RefreshManager manager) {
		super(Policy.bind("refresh.pollJob")); //$NON-NLS-1$
		this.manager = manager;
		setPriority(Job.LONG);
		setSystem(true);
		resourceRoots = new ArrayList();
	}
	/**
	 * Add the given root to the list of roots that need to be polled.
	 */
	public synchronized void monitor(IResource root) {
		resourceRoots.add(root);
		schedule(pollingDelay);
	}
	/**
	 * Polls the filesystem under the root containers for changes.
	 */
	protected IStatus run(IProgressMonitor monitor) {
		long start = System.currentTimeMillis();
		try {
			if (RefreshManager.DEBUG)
				System.out.println(RefreshManager.DEBUG_PREFIX+"started polling"); //$NON-NLS-1$
			monitor.beginTask("", resourceRoots.size()); //$NON-NLS-1$
			IResource[] roots = getRoots();
			for (int i = 0; i < roots.length; i++) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				if (!roots[i].isSynchronized(IResource.DEPTH_INFINITE))
					manager.refresh(roots[i]);
				monitor.worked(1);
			}
		} finally {
			monitor.done();
			if (RefreshManager.DEBUG)
				System.out.println(RefreshManager.DEBUG_PREFIX+"finished polling in: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		//reschedule automatically - shouldRun will cancel if not needed
		schedule(pollingDelay);
		return Status.OK_STATUS;
	}
	public void setPollingDelay(long delay) {
		this.pollingDelay = delay;
	}
	public boolean shouldRun() {
		return !resourceRoots.isEmpty();
	}
	/**
	 * Returns a copy of the resources to be polled (thread safety).
	 */
	private synchronized IResource[] getRoots() {
		return (IResource[]) resourceRoots.toArray(new IResource[resourceRoots.size()]);
	}
	/*
	 * @see org.eclipse.core.resources.refresh.IRefreshMonitor#unmonitor(IContainer)
	 */
	public synchronized void unmonitor(IResource resource) {
		if (resource == null) 
			resourceRoots.clear();
		else 
			resourceRoots.remove(resource);
		if (resourceRoots.isEmpty())
			cancel();
	}
}