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
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.internal.progress.PendingUpdateAdapter;


/**
 * DeferredContentProvider is largely adapted from (@see
 * org.eclipse.wst.common.navigator.views.DeferredTreeContentManager)
 * 
 * @author Michael D. Elder <mdelder@us.ibm.com>
 */
public class DeferredContentProvider implements ITreeContentProvider {

	private IPendingElementCollector collector = null;

	private DeferredAdapterProvider provider = null;

	private AbstractTreeViewer viewer;

	/**
	 *  
	 */
	public DeferredContentProvider(IPendingElementCollector collector, DeferredAdapterProvider provider) {
		super();
		this.collector = collector;
		this.provider = provider;
	}

	/**
	 * Returns the child elements of the given element, or in the case of a deferred element,
	 * returns a placeholder. If a deferred element used a job is created to fetch the children in
	 * the background.
	 * 
	 * @param parent
	 *            The parent object.
	 * @return Object[] or <code>null</code> if parent is not an instance of
	 *         IDeferredWorkbenchAdapter.
	 */
	public Object[] getChildren(Object element) {

		// TODO MDE Change made due to the changes in the Job Scheduler.
		return this.provider.getChildren(element);
		/*
		 * if (this.viewer != null) { if (this.viewer.getExpandedState(element)) return
		 * this.provider.getChildren(element); } IDeferredElementAdapter[] adapters =
		 * this.provider.getDeferredAdapters(element); if (adapters != null && adapters.length > 0) {
		 * PendingUpdateAdapter pendingElement = new PendingUpdateAdapter();
		 * startFetchingDeferredChildren(adapters, element, pendingElement); return new Object[] {
		 * pendingElement }; } return new Object[0];
		 */
	}

	public Object[] getChildren(Object element, boolean isDeferred) {
		// Change due to Job scheduler
		if (isDeferred && false)
			return getChildren(element);
		return this.provider.getChildren(element);
	}

	/**
	 * Starts a job and creates a collector for fetching the children of this deferred adapter. If
	 * children are waiting to be retrieve for this parent already, that job is cancelled and
	 * another is started.
	 * 
	 * @param parent.
	 *            The parent object being filled in,
	 * @param adapter
	 *            The adapter being used to fetch the children.
	 * @param placeholder
	 *            The adapter that will be used to indicate that results are pending.
	 */
	protected void startFetchingDeferredChildren(IDeferredElementAdapter[] adapters, Object element, final PendingUpdateAdapter pendingElement) {

		// Cancel any jobs currently fetching children for the same parent
		// instance.
		//Platform.getJobManager().cancel(adapter.getRule(element));
		if (adapters.length > 0) {
			Job fetchElementsJob = new FetchElementsJob(element, collector, adapters);
			fetchElementsJob.addJobChangeListener(new JobChangeAdapter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
				 */
				public void done(IJobChangeEvent event) {
					collector.done(pendingElement);
				}

			});

			fetchElementsJob.schedule();
		}
	}

	/**
	 * Return the IDeferredWorkbenchAdapter for element or the element if it is an instance of
	 * IDeferredWorkbenchAdapter. If it does not exist return null.
	 * 
	 * @param element
	 * @return IDeferredWorkbenchAdapter or <code>null</code>
	 */
	protected IDeferredElementAdapter getAdapter(Object element) {

		if (element instanceof IDeferredElementAdapter)
			return (IDeferredElementAdapter) element;

		if (!(element instanceof IAdaptable))
			return null;

		Object adapter = ((IAdaptable) element).getAdapter(IDeferredElementAdapter.class);
		if (adapter == null)
			return null;
		return (IDeferredElementAdapter) adapter;

	}

	/**
	 * Return whether or not the element is or adapts to an IDeferredWorkbenchAdapter.
	 * 
	 * @param element
	 * @return boolean <code>true</code> if the element is an IDeferredWorkbenchAdapter
	 */
	public boolean isDeferredAdapter(Object element) {
		return getAdapter(element) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewerArg, Object oldInput, Object newInput) {
		if (viewerArg instanceof AbstractTreeViewer)
			this.viewer = (AbstractTreeViewer) viewerArg;
		provider.inputChanged(viewerArg, oldInput, newInput);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		provider.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object element) {
		return provider.getElements(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return provider.getParent(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		return provider.hasChildren(element);
	}

	/**
	 * @return Returns the viewer.
	 */
	protected AbstractTreeViewer getViewer() {
		return viewer;
	}
}