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
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;

public class MergeSubscriberContext extends CVSSubscriberMergeContext {

	public static IMergeContext createContext(IResourceMappingScope scope, Subscriber subscriber, IProgressMonitor monitor) throws CoreException {
		MergeSubscriberContext mergeContext = new MergeSubscriberContext(subscriber, scope);
		mergeContext.initialize(monitor, true);
		return mergeContext;
	}
	
	public MergeSubscriberContext(Subscriber subscriber, IResourceMappingScope scope) {
		super(subscriber, scope);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IMergeContext#markAsMerged(org.eclipse.team.core.diff.IDiffNode, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void markAsMerged(final IDiff node, boolean inSyncHint, IProgressMonitor monitor) throws CoreException {
		run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				((CVSMergeSubscriber)getSubscriber()).merged(new IResource[] { ((IResourceDiff)node).getResource() });
			}
		}, getMergeRule(node), IResource.NONE, monitor);
	}
	
	public void markAsMerged(final IDiff[] nodes, boolean inSyncHint, IProgressMonitor monitor) throws CoreException {
		run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				List result = new ArrayList();
				for (int i = 0; i < nodes.length; i++) {
					IDiff node = nodes[i];
					result.add(((IResourceDiff)node).getResource());
				}
				((CVSMergeSubscriber)getSubscriber()).merged((IResource[]) result.toArray(new IResource[result.size()]));
			}
		}, getMergeRule(nodes), IResource.NONE, monitor);
	}
	
	public void dispose() {
		((CVSMergeSubscriber)getSubscriber()).cancel();
		super.dispose();
	}

}
