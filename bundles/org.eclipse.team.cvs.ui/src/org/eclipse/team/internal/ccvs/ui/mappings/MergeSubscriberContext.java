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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;

public class MergeSubscriberContext extends CVSSubscriberMergeContext {

	public static MergeSubscriberContext createContext(ISynchronizationScopeManager manager, Subscriber subscriber) {
		MergeSubscriberContext mergeContext = new MergeSubscriberContext(subscriber, manager);
		mergeContext.initialize();
		return mergeContext;
	}
	
	public MergeSubscriberContext(Subscriber subscriber, ISynchronizationScopeManager manager) {
		super(subscriber, manager);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IMergeContext#markAsMerged(org.eclipse.team.core.diff.IDiffNode, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void markAsMerged(final IDiff diff, boolean inSyncHint, IProgressMonitor monitor) throws CoreException {
		run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				((CVSMergeSubscriber)getSubscriber()).merged(new IResource[] { getDiffTree().getResource(diff)});
			}
		}, getMergeRule(diff), IResource.NONE, monitor);
	}
	
	public void markAsMerged(final IDiff[] diffs, boolean inSyncHint, IProgressMonitor monitor) throws CoreException {
		run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				List result = new ArrayList();
				for (int i = 0; i < diffs.length; i++) {
					IDiff diff = diffs[i];
					result.add(getDiffTree().getResource(diff));
				}
				((CVSMergeSubscriber)getSubscriber()).merged((IResource[]) result.toArray(new IResource[result.size()]));
			}
		}, getMergeRule(diffs), IResource.NONE, monitor);
	}
	
	public void dispose() {
		((CVSMergeSubscriber)getSubscriber()).cancel();
		super.dispose();
	}

}
