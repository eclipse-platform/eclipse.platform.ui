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
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.IResourceMappingScope;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberMergeContext;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;

public abstract class CVSSubscriberMergeContext extends SubscriberMergeContext {

	protected CVSSubscriberMergeContext(Subscriber subscriber, IResourceMappingScope scope) {
		super(subscriber, scope);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.MergeContext#run(org.eclipse.core.resources.IWorkspaceRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(final IWorkspaceRunnable runnable, final ISchedulingRule rule, int flags, IProgressMonitor monitor) throws CoreException {
		super.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				EclipseSynchronizer.getInstance().run(rule, new ICVSRunnable(){
					public void run(IProgressMonitor monitor) throws CVSException {
						try {
							runnable.run(monitor);
						} catch (CoreException e) {
							throw CVSException.wrapException(e);
						}
					}
				}, monitor);
			}
		
		}, rule, flags, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.MergeContext#getMergeRule(org.eclipse.core.resources.IResource)
	 */
	public ISchedulingRule getMergeRule(IDiff node) {
		// Return the project since that is what the EclipseSynchronize needs
		return getDiffTree().getResource(node).getProject();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.provider.MergeContext#makeInSync(org.eclipse.team.core.diff.IDiff, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void makeInSync(IDiff diff, IProgressMonitor monitor) throws CoreException {
		markAsMerged(diff, true, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IMergeContext#reject(org.eclipse.team.core.diff.IDiff, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void reject(IDiff diff, IProgressMonitor monitor) throws CoreException {
		markAsMerged(diff, false, monitor);
	}

}
