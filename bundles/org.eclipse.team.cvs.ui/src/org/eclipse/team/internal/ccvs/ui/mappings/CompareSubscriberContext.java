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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.diff.DiffFilter;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.ccvs.core.CVSCompareSubscriber;
import org.eclipse.team.internal.core.subscribers.ContentComparisonDiffFilter;

public class CompareSubscriberContext extends CVSSubscriberMergeContext {

	public static SynchronizationContext createContext(ISynchronizationScopeManager manager, CVSCompareSubscriber subscriber) {
		CompareSubscriberContext mergeContext = new CompareSubscriberContext(subscriber, manager);
		mergeContext.initialize();
		return mergeContext;
	}
	
	protected CompareSubscriberContext(Subscriber subscriber, ISynchronizationScopeManager manager) {
		super(subscriber, manager);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IMergeContext#markAsMerged(org.eclipse.team.core.diff.IDiff, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void markAsMerged(IDiff node, boolean inSyncHint,
			IProgressMonitor monitor) throws CoreException {
		// Do nothing 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.SubscriberMergeContext#getDiffFilter()
	 */
	protected DiffFilter getDiffFilter() {
		// Return a filter that selects any diffs whose contents are not equal
		final DiffFilter contentsEqual = new ContentComparisonDiffFilter(false);
		return new DiffFilter() {
			public boolean select(IDiff diff, IProgressMonitor monitor) {
				return !contentsEqual.select(diff, monitor);
			}
		};
	}

}
