/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.progress.IElementCollector;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This adapter interface provides support for lazy initialization of UI workbench elements
 * that are displayed visually. This adapter is used with an associated deferred content provider.
 * 
 * @see DeferredWorkbenchContentProvider
 * @see BatchSimilarSchedulingRule
 */
public interface IDeferredWorkbenchAdapter extends IWorkbenchAdapter {
		
	/**
	 * Called by a job run in a separate thread to fetch the children of this adapter.
	 * The adapter should return notify of new children via the collector.
	 * <p>
	 * It is good practice to check the passed in monitor for cancellation. This will 
	 * provide good responsiveness for cancellation requests made by the user.
	 * </p>
	 * 
	 * @param o the object to fetch the children for
	 * @param collector the collector to notify about new children
	 * @param  monitor a progress monitor that will never be <code>null<code> to
 	 *                   support reporting and cancellation.
	 */
	public void fetchDeferredChildren(Object o, IElementCollector collector, IProgressMonitor monitor);

	/**
	 * Returns whether this adapter may have children. This is an optimized method
	 * used by content providers to allow showing the [+] expand icon without having
	 * yet fetched the children for the element.
	 * <p>
	 * If <code>false</code> is returned, then the content provider is free to assume
	 * that this adapter has no children. If <code>true</code> is returned, 
	 * then the job manager is free to assume that this adapter may have children.
	 * <p>
	 * 
	 * @param rule the rule to check for conflicts
	 * @return <code>true</code> if the rule is conflicting, and <code>false</code>
	 * 	otherwise.
	 */
	public boolean isContainer();
	
	/**
	 * Returns the rule used to schedule the deferred fetching of children for this adapter. The job manager 
	 * guarantees that no two jobs with conflicting scheduling rules will run concurrently.
	 * 
	 * @return ISchedulingRule the scheduling rule 
	 */
	public ISchedulingRule getRule();
}