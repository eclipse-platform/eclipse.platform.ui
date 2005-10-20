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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.navigator.internal.NavigatorMessages;


/**
 * @author Administrator
 * 
 *  
 */
public class FetchElementsJob extends Job {

	private Object element = null;

	private IPendingElementCollector elementCollector = null;

	private IDeferredElementAdapter[] deferredAdapters = null;

	/**
	 *  
	 */
	public FetchElementsJob(Object element, IPendingElementCollector elementCollector, IDeferredElementAdapter[] deferredAdapters) {
		this("Fetching children of {0}", element, elementCollector, deferredAdapters); //$NON-NLS-1$
	}

	public FetchElementsJob(String name, Object element, IPendingElementCollector elementCollector, IDeferredElementAdapter[] deferredAdapters) {
		super(name);

		this.element = element;
		this.elementCollector = elementCollector;
		this.deferredAdapters = deferredAdapters;
	}

	public IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(NavigatorMessages.getString("FetchElementsJob.0"), deferredAdapters.length); //$NON-NLS-1$

		Object[] children = null;
		for (int i = 0; i < this.deferredAdapters.length; i++) {
			children = this.deferredAdapters[i].getChildren(element);
			if (children != null && children.length != 0)
				this.elementCollector.collectChildren(element, children);
			monitor.worked(1);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Check if the object is equal to parent or one of parents children so that the job can be
	 * cancelled if the parent is refreshed.
	 * 
	 * @param ancestor
	 *            the potential ancestor of the current parent
	 * @return boolean
	 */
	public boolean belongsTo(Object ancestor) {
		return isParent(ancestor, element);
	}

	/**
	 * Check if the parent of element is equal to the parent used in this job.
	 * 
	 * @param ancestor.
	 *            The potential ancestor of the current parent
	 * @param child.
	 *            The object to check against.
	 * @return boolean
	 */
	private boolean isParent(Object family, Object child) {
		if (family.equals(child))
			return true;
		IWorkbenchAdapter workbenchAdapter = getWorkbenchAdapter(child);
		if (workbenchAdapter == null)
			return false;
		Object elementParent = workbenchAdapter.getParent(child);
		if (elementParent == null)
			return false;
		return isParent(family, elementParent);
	}

	/**
	 * Get the workbench adapter for the element.
	 * 
	 * @param elementArg.
	 *            The object we are adapting to.
	 */
	private IWorkbenchAdapter getWorkbenchAdapter(Object elementArg) {
		if (elementArg instanceof IWorkbenchAdapter)
			return (IWorkbenchAdapter) elementArg;
		if (!(elementArg instanceof IAdaptable))
			return null;
		Object workbenchAdapter = ((IAdaptable) elementArg).getAdapter(IWorkbenchAdapter.class);
		if (workbenchAdapter == null)
			return null;
		return (IWorkbenchAdapter) workbenchAdapter;
	}

}