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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.progress.IElementCollector;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.PendingUpdateAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.progress.UIJob;

/**
 * Provides lazy contents retrieval for objects that have the IDeferredWorkbenchAdapter 
 * adapter registered.
 * 
 * @see IDeferredWorkbenchAdapter
 */
public class DeferredWorkbenchContentProvider extends WorkbenchContentProvider {

	final static boolean ENABLE_BACKGROUND_FETCHING = false;

	/** 
	 * The wrapper is required to ensure that we cancel fetching children jobs for
	 * the same parent instance that were created from this content provider and not
	 * those created by others.
	 */
	private class FamilyWrapper {
		Object o;
		FamilyWrapper(Object o) {
			this.o = o;
		}
	}

	/** 
	 * This job is used to add elements to a viewer.
	 */
	private class AddElementsToViewJob extends UIJob {
		Object[] newElements;
		Object parent;
		private boolean working;

		AddElementsToViewJob(Object parentElement) {
			super(viewer.getControl().getDisplay());
			parent = parentElement;
			addJobChangeListener(new JobChangeAdapter() {
				public void done(Job job, IStatus result) {
					setWorking(false);
				}
			});
		}
		synchronized boolean isWorking() {
			return working;
		}
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			if (viewer instanceof AbstractTreeViewer) {
				((AbstractTreeViewer) viewer).add(parent, newElements);
			}
			return Status.OK_STATUS;
		}
		void runBatch(Object[] elements) {
			working = true;
			this.newElements = elements;
			schedule();
		}
		synchronized void setWorking(boolean working) {
			this.working = working;
		}
	}

	/** 
	 * This collector is responsible for updating the viewer when deferred children are fetched by
	 * a deferred adapter. Elements added to the viewer are batched so that the viewer doesn't
	 * do too much work in the UI thread.
	 * 
	 * TODO: this currently doesn't batch calls to add(Object). 
	 */
	abstract private class DeferredElementCollector implements IElementCollector {

		// The number of elements to add to the view at a time. While these elements are
		// added the UI thread is locked.
		private int BATCH_SIZE = 5;

		// The viewer in which to add new elements.
		private Viewer viewer;

		private boolean DEBUG = true;
		private long FAKE_LATENCY = 100; // milliseconds

		private PendingUpdateAdapter placeholder;

		public DeferredElementCollector(Viewer viewer, PendingUpdateAdapter placeholder) {
			this.viewer = viewer;
			this.placeholder = placeholder;			
		}

		public void addChildren(final Object parent, final Object[] children, IProgressMonitor monitor) {
			monitor = Policy.monitorFor(monitor);
			if (viewer instanceof AbstractTreeViewer) {
				final AbstractTreeViewer treeViewer = (AbstractTreeViewer) viewer;

				if (placeholder != null) {
					UIJob removalJob = new UIJob() {
						public IStatus runInUIThread(IProgressMonitor monitor) {
							treeViewer.remove(placeholder);
							placeholder = null;
							return Status.OK_STATUS;
						}
					};
					removalJob.schedule();

				}
				AddElementsToViewJob addElementsJob = new AddElementsToViewJob(parent);
				if (children.length == 0) {
					addElementsJob.runBatch(children);
					return;
				}

				int batchStart = 0;
				int batchEnd = children.length > BATCH_SIZE ? BATCH_SIZE : children.length;
				//process children until all children have been sent to the UI
				while (batchStart < children.length) {
					if (monitor.isCanceled()) {
						addElementsJob.cancel();
						return;
					}

					if (DEBUG)
						slowDown(FAKE_LATENCY);

					//only send a new batch when the last batch is finished
					if (!addElementsJob.isWorking()) {
						int batchLength = batchEnd - batchStart;
						Object[] batch = new Object[batchLength];
						System.arraycopy(children, batchStart, batch, 0, batchLength);

						addElementsJob.runBatch(batch);

						batchStart += batchLength;
						batchEnd = (batchStart + BATCH_SIZE);
						if (batchEnd >= children.length) {
							batchEnd = children.length;
						}
					}
				}
			} else {
				viewer.refresh();
			}
		}

		private void slowDown(long time) {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Provides an optimized lookup for determining if an element has children. This is
	 * required because elements that are populated lazilly can't answer <code>getChildren</code>
	 * just to determine the potential for children.
	 */
	public boolean hasChildren(Object o) {
		if (o == null) {
			return false;
		}
		IWorkbenchAdapter adapter = getAdapter(o);
		if (adapter instanceof IDeferredWorkbenchAdapter) {
			IDeferredWorkbenchAdapter element = (IDeferredWorkbenchAdapter) adapter;
			return element.isContainer();
		}
		return super.hasChildren(o);
	}

	/**
	 * Returns the child elements of the given element, or in the case of a deferred element, returns
	 * a placeholder. If a deferred element used a job is created to fetch the children in the background.
	 */
	public Object[] getChildren(final Object parent) {
		IWorkbenchAdapter adapter = getAdapter(parent);
		if (adapter instanceof IDeferredWorkbenchAdapter && ENABLE_BACKGROUND_FETCHING) {
			IDeferredWorkbenchAdapter element = (IDeferredWorkbenchAdapter) adapter;
			return startFetchingDeferredChildren(parent, element);
		}
		return super.getChildren(parent);
	}

	/**
	 * Starts a job and creates a collector for fetching the children of this deferred adapter. If children
	 * are waiting to be retrieve for this parent already, that job is cancelled and another is started.
	 */
	private Object[] startFetchingDeferredChildren(final Object parent, final IDeferredWorkbenchAdapter adapter) {

		PendingUpdateAdapter pendingPlaceHolder = new PendingUpdateAdapter();
		final DeferredElementCollector collector = new DeferredElementCollector(viewer, pendingPlaceHolder) {
			public void add(Object element, IProgressMonitor monitor) {
				add(new Object[] { element }, monitor);
			}
			public void add(Object[] elements, IProgressMonitor monitor) {
				addChildren(parent, elements, monitor);
			}
		};

		// Cancel any jobs currently fetching children for the same parent instance.
		Platform.getJobManager().cancel(new FamilyWrapper(parent));
		Job job = new Job() {
			public IStatus run(IProgressMonitor monitor) {
				try {
					adapter.fetchDeferredChildren(parent, collector, monitor);
				} catch (OperationCanceledException e) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
			public boolean belongsTo(Object family) {
				if (family instanceof FamilyWrapper) {
					return ((FamilyWrapper) family).o == parent;
				}
				return false;
			}
		};

		job.setRule(adapter.getRule());
		job.schedule();
		return new Object[] {pendingPlaceHolder};
	}
}