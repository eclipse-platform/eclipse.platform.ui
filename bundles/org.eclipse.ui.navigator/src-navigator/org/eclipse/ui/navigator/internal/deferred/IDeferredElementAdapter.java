/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.deferred;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This adapter interface provides support for lazy initialization of UI workbench elements that are
 * displayed visually. This adapter is used with an associated deferred content provider.
 * 
 * @see DeferredAdapterProvider
 * @since 3.0
 * 
 * <b>NOTE: </b> This API is experimental and subject to change.
 */
public interface IDeferredElementAdapter extends IWorkbenchAdapter {

	/**
	 * Called by a job run in a separate thread to fetch the children of this adapter. The adapter
	 * should return notify of new children via the collector.
	 * <p>
	 * It is good practice to check the passed in monitor for cancellation. This will provide good
	 * responsiveness for cancellation requests made by the user.
	 * </p>
	 * 
	 * @param object
	 *            the object to fetch the children for
	 * @param collector
	 *            the collector to notify about new children
	 * @param monitor
	 *            a progress monitor that will never be <code>null<code> to
	 *                   support reporting and cancellation.
	 */
	public Object[] getChildren(Object element);

	/**
	 * Returns whether this adapter may have children. This is an optimized method used by content
	 * providers to allow showing the [+] expand icon without having yet fetched the children for
	 * the element.
	 * <p>
	 * If <code>false</code> is returned, then the content provider may assume that this adapter
	 * has no children. If <code>true</code> is returned, then the job manager may assume that
	 * this adapter may have children.
	 * <p>
	 * 
	 * @return <code>true</code> if the rule is conflicting, and <code>false</code> otherwise.
	 */
	public boolean isContainer();

	/**
	 * Returns the rule used to schedule the deferred fetching of children for this adapter.
	 * 
	 * @param object
	 *            the object whose children are being fetched
	 * @return ISchedulingRule the scheduling rule
	 */
	public ISchedulingRule getRule(Object object);
}