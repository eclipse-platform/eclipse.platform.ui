/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.examples.filesystem.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.subscribers.SubscriberMergeContext;

/**
 * A merge context for merging file system changes.
 */
public class FileSystemMergeContext extends SubscriberMergeContext {

	/**
	 * Create the file system merge context for the given scope manager.
	 * @param manager the scope manager
	 */
	public FileSystemMergeContext(ISynchronizationScopeManager manager) {
		super(FileSystemSubscriber.getInstance(), manager);
		initialize();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.provider.MergeContext#makeInSync(org.eclipse.team.core.diff.IDiff, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void makeInSync(IDiff diff, IProgressMonitor monitor)
			throws CoreException {
		IResource resource = ResourceDiffTree.getResourceFor(diff);
		FileSystemSubscriber.getInstance().makeInSync(resource);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IMergeContext#markAsMerged(org.eclipse.team.core.diff.IDiff, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void markAsMerged(IDiff diff, boolean inSyncHint,
			IProgressMonitor monitor) throws CoreException {
		// TODO if inSyncHint is true, we should test to see if the contents match
		IResource resource = ResourceDiffTree.getResourceFor(diff);
		FileSystemSubscriber.getInstance().markAsMerged(resource, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IMergeContext#reject(org.eclipse.team.core.diff.IDiff, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void reject(IDiff diff, IProgressMonitor monitor)
			throws CoreException {
		markAsMerged(diff, false, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.provider.MergeContext#getMergeRule(org.eclipse.team.core.diff.IDiff)
	 */
	public ISchedulingRule getMergeRule(IDiff node) {
		return ResourceDiffTree.getResourceFor(node).getProject();
	}
	

}
