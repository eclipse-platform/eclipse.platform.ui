/*******************************************************************************
 * Copyright (c) 2003, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The DeferredContentManager is a class that helps an ITreeContentProvider get
 * its deferred input.
 * 
 * <b>NOTE</b> AbstractTreeViewer#isExpandable may need to be implemented in
 * AbstractTreeViewer subclasses with deferred content that use filtering as a
 * call to #getChildren may be required to determine the correct state of the
 * expanding control.
 * 
 * AbstractTreeViewers which use this class may wish to sacrifice accuracy of
 * the expandable state indicator for the performance benefits of deferring
 * content.
 * 
 * @see IDeferredWorkbenchAdapter
 * @since 3.0
 */
public class DeferredTreeContentManager {

	AbstractTreeViewer treeViewer;

	IWorkbenchSiteProgressService progressService;

	private ListenerList updateCompleteListenerList;

	/**
	 * The DeferredContentFamily is a class used to keep track of a
	 * manager-object pair so that only jobs scheduled by the receiver are
	 * canceled by the receiver.
	 * 
	 * @since 3.1
	 * 
	 */
	class DeferredContentFamily {
		protected DeferredTreeContentManager manager;
		protected Object element;

		/**
		 * Create a new instance of the receiver to define a family for object
		 * in a particular scheduling manager.
		 * 
		 * @param schedulingManager
		 * @param object
		 */
		DeferredContentFamily(DeferredTreeContentManager schedulingManager,
				Object object) {
			this.manager = schedulingManager;
			this.element = object;
		}
	}

	/**
	 * Create a new instance of the receiver using the supplied content provider
	 * and viewer. Run any jobs using the site.
	 * 
	 * @param provider
	 * @param viewer
	 * @param site
	 * @deprecated in 3.4. provider is not used by this class
	 */
	public DeferredTreeContentManager(ITreeContentProvider provider,
			AbstractTreeViewer viewer, IWorkbenchPartSite site) {
		this(viewer, site);
	}

	/**
	 * Create a new instance of the receiver using the supplied content provider
	 * and viewer.
	 * 
	 * @param provider
	 *            The content provider that will be updated
	 * @param viewer
	 *            The tree viewer that the results are added to
	 * @deprecated in 3.4. provider is not used by this class
	 */
	public DeferredTreeContentManager(ITreeContentProvider provider,
			AbstractTreeViewer viewer) {
		this(viewer);
	}

	/**
	 * Create a new instance of the receiver using the supplied content provider
	 * and viewer. Run any jobs using the site.
	 * 
	 * @param viewer
	 * @param site
	 * 
	 * @since 3.4
	 */
	public DeferredTreeContentManager(AbstractTreeViewer viewer,
			IWorkbenchPartSite site) {
		this(viewer);
		Object siteService = Util.getAdapter(site,
				IWorkbenchSiteProgressService.class);
		if (siteService != null) {
			progressService = (IWorkbenchSiteProgressService) siteService;
		}
	}

	/**
	 * Create a new instance of the receiver using the supplied content provider
	 * and viewer.
	 * 
	 * @param viewer
	 *            The tree viewer that the results are added to
	 * 
	 * @since 3.4
	 */
	public DeferredTreeContentManager(AbstractTreeViewer viewer) {
		treeViewer = viewer;
	}

	/**
	 * Provides an optimized lookup for determining if an element has children.
	 * This is required because elements that are populated lazilly can't answer
	 * <code>getChildren</code> just to determine the potential for children.
	 * Throw an AssertionFailedException if element is null.
	 * 
	 * @param element
	 *            The Object being tested. This should not be <code>null</code>.
	 * @return boolean <code>true</code> if there are potentially children.
	 * @throws RuntimeException
	 *             if the element is null.
	 */
	public boolean mayHaveChildren(Object element) {
		Assert.isNotNull(element,
				ProgressMessages.DeferredTreeContentManager_NotDeferred);
		IDeferredWorkbenchAdapter adapter = getAdapter(element);
		return adapter != null && adapter.isContainer();
	}

	/**
	 * Returns the child elements of the given element, or in the case of a
	 * deferred element, returns a placeholder. If a deferred element is used, a
	 * job is created to fetch the children in the background.
	 * 
	 * @param parent
	 *            The parent object.
	 * @return Object[] or <code>null</code> if parent is not an instance of
	 *         IDeferredWorkbenchAdapter.
	 */
	public Object[] getChildren(final Object parent) {
		IDeferredWorkbenchAdapter element = getAdapter(parent);
		if (element == null) {
			return null;
		}
		PendingUpdateAdapter placeholder = createPendingUpdateAdapter();
		startFetchingDeferredChildren(parent, element, placeholder);
		return new Object[] { placeholder };
	}

	/**
	 * Factory method for creating the pending update adapter representing the
	 * placeholder node. Subclasses may override.
	 * 
	 * @return a pending update adapter
	 * @since 3.2
	 */
	protected PendingUpdateAdapter createPendingUpdateAdapter() {
		return new PendingUpdateAdapter();
	}

	/**
	 * Return the IDeferredWorkbenchAdapter for element or the element if it is
	 * an instance of IDeferredWorkbenchAdapter. If it does not exist return
	 * null.
	 * 
	 * @param element
	 * @return IDeferredWorkbenchAdapter or <code>null</code>
	 */
	protected IDeferredWorkbenchAdapter getAdapter(Object element) {
		return (IDeferredWorkbenchAdapter) Util.getAdapter(element,
				IDeferredWorkbenchAdapter.class);
	}

	/**
	 * Starts a job and creates a collector for fetching the children of this
	 * deferred adapter. If children are waiting to be retrieved for this parent
	 * already, that job is cancelled and another is started.
	 * 
	 * @param parent
	 *            The parent object being filled in,
	 * @param adapter
	 *            The adapter being used to fetch the children.
	 * @param placeholder
	 *            The adapter that will be used to indicate that results are
	 *            pending.
	 */
	protected void startFetchingDeferredChildren(final Object parent,
			final IDeferredWorkbenchAdapter adapter,
			final PendingUpdateAdapter placeholder) {
		final IElementCollector collector = createElementCollector(parent,
				placeholder);
		// Cancel any jobs currently fetching children for the same parent
		// instance.
		cancel(parent);
		String jobName = getFetchJobName(parent, adapter);
		Job job = new Job(jobName) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus run(IProgressMonitor monitor) {
				adapter.fetchDeferredChildren(parent, collector, monitor);
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.jobs.Job#belongsTo(java.lang.Object)
			 */
			public boolean belongsTo(Object family) {
				if (family instanceof DeferredContentFamily) {
					DeferredContentFamily contentFamily = (DeferredContentFamily) family;
					if (contentFamily.manager == DeferredTreeContentManager.this) {
						return isParent(contentFamily, parent);
					}
				}
				return false;

			}

			/**
			 * Check if the parent of element is equal to the parent used in
			 * this job.
			 * 
			 * @param family
			 *            The DeferredContentFamily that defines a potential
			 *            ancestor of the current parent in a particular
			 *            manager.
			 * @param child
			 *            The object to check against.
			 * @return boolean <code>true</code> if the child or one of its
			 *         parents are the same as the element of the family.
			 */
			private boolean isParent(DeferredContentFamily family, Object child) {
				if (family.element.equals(child)) {
					return true;
				}
				IWorkbenchAdapter workbenchAdapter = getWorkbenchAdapter(child);
				if (workbenchAdapter == null) {
					return false;
				}
				Object elementParent = workbenchAdapter.getParent(child);
				if (elementParent == null) {
					return false;
				}
				return isParent(family, elementParent);
			}

			/**
			 * Get the workbench adapter for the element.
			 * 
			 * @param element
			 *            The object we are adapting to.
			 */
			private IWorkbenchAdapter getWorkbenchAdapter(Object element) {
				return (IWorkbenchAdapter) Util.getAdapter(element,
						IWorkbenchAdapter.class);
			}
		};
		job.addJobChangeListener(new JobChangeAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void done(IJobChangeEvent event) {
				runClearPlaceholderJob(placeholder);
			}
		});
		job.setRule(adapter.getRule(parent));
		if (progressService == null) {
			job.schedule();
		} else {
			progressService.schedule(job);
		}
	}

	/**
	 * Returns a name to use for the job that fetches children of the given
	 * parent. Subclasses may override. Default job name is parent's label.
	 * 
	 * @param parent
	 *            parent that children are to be fetched for
	 * @param adapter
	 *            parent's deferred adapter
	 * @return job name
	 */
	protected String getFetchJobName(Object parent,
			IDeferredWorkbenchAdapter adapter) {
		return NLS.bind(
				ProgressMessages.DeferredTreeContentManager_FetchingName,
				adapter.getLabel(parent));
	}

	/**
	 * Create a UIJob to add the children to the parent in the tree viewer.
	 * 
	 * @param parent
	 * @param children
	 * @param monitor
	 */
	protected void addChildren(final Object parent, final Object[] children,
			IProgressMonitor monitor) {
		WorkbenchJob updateJob = new WorkbenchJob(
				ProgressMessages.DeferredTreeContentManager_AddingChildren) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor updateMonitor) {
				// Cancel the job if the tree viewer got closed
				if (treeViewer.getControl().isDisposed()
						|| updateMonitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				treeViewer.add(parent, children);
				return Status.OK_STATUS;
			}
		};
		updateJob.setSystem(true);
		updateJob.schedule();

	}

	/**
	 * Return whether or not the element is or adapts to an
	 * IDeferredWorkbenchAdapter.
	 * 
	 * @param element
	 * @return boolean <code>true</code> if the element is an
	 *         IDeferredWorkbenchAdapter
	 */
	public boolean isDeferredAdapter(Object element) {
		return getAdapter(element) != null;
	}

	/**
	 * Run a job to clear the placeholder. This is used when the update for the
	 * tree is complete so that the user is aware that no more updates are
	 * pending.
	 * 
	 * @param placeholder
	 */
	protected void runClearPlaceholderJob(final PendingUpdateAdapter placeholder) {
		if (placeholder.isRemoved() || !PlatformUI.isWorkbenchRunning()) {
			return;
		}
		// Clear the placeholder if it is still there
		WorkbenchJob clearJob = new WorkbenchJob(
				ProgressMessages.DeferredTreeContentManager_ClearJob) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!placeholder.isRemoved()) {
					Control control = treeViewer.getControl();
					if (control.isDisposed()) {
						return Status.CANCEL_STATUS;
					}
					treeViewer.remove(placeholder);
					placeholder.setRemoved(true);
				}
				return Status.OK_STATUS;
			}
		};
		clearJob.setSystem(true);
		
		if (updateCompleteListenerList != null) {
			Object[] listeners = updateCompleteListenerList.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				clearJob
						.addJobChangeListener((IJobChangeListener) listeners[i]);
			}
		}
		clearJob.schedule();
	}

	/**
	 * Cancel all jobs that are fetching content for the given parent or any of
	 * its children.
	 * 
	 * @param parent
	 */
	public void cancel(Object parent) {
		if (parent == null) {
			return;
		}

		Job.getJobManager().cancel(new DeferredContentFamily(this, parent));
	}

	/**
	 * Create the element collector for the receiver.
	 * 
	 * @param parent
	 *            The parent object being filled in,
	 * @param placeholder
	 *            The adapter that will be used to indicate that results are
	 *            pending.
	 * @return IElementCollector
	 */
	protected IElementCollector createElementCollector(final Object parent,
			final PendingUpdateAdapter placeholder) {
		return new IElementCollector() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.progress.IElementCollector#add(java.lang.Object,
			 *      org.eclipse.core.runtime.IProgressMonitor)
			 */
			public void add(Object element, IProgressMonitor monitor) {
				add(new Object[] { element }, monitor);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.progress.IElementCollector#add(java.lang.Object[],
			 *      org.eclipse.core.runtime.IProgressMonitor)
			 */
			public void add(Object[] elements, IProgressMonitor monitor) {
				addChildren(parent, elements, monitor);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.progress.IElementCollector#done()
			 */
			public void done() {
				runClearPlaceholderJob(placeholder);
			}
		};
	}

	/**
	 * Add a listener to list of update complete listeners. These listeners are
	 * attached to the job that updates the viewer content (clears the pending
	 * entry, etc.) after all deferred content has been retrieved.
	 * 
	 * This method has no effect if the listener has already been added to the
	 * list of listeners.
	 * 
	 * Since 3.6, this listener is added to a list of listeners rather than
	 * replacing the previously added listener. For backward compatibility,
	 * adding a null listener will be interpreted as removal of a listener if
	 * only one listener has been registered.
	 * 
	 * @param listener
	 *            the listener to add to the list of update listeners
	 * @since 3.4
	 */
	public void addUpdateCompleteListener(IJobChangeListener listener){
		// Maintain backward compatibility.
		// Earlier only one listener was supported, so it can be removed by
		// passing null
		if (listener == null && updateCompleteListenerList != null) {
			Object[] listeners = updateCompleteListenerList.getListeners();
			if (listeners.length == 1) {
				removeUpdateCompleteListener((IJobChangeListener) listeners[0]);
			}
		} else {
			if (updateCompleteListenerList == null) {
				updateCompleteListenerList = new ListenerList();
			}
			updateCompleteListenerList.add(listener);
		}
	}

	/**
	 * Removes the listener from the list of update listeners that are attached
	 * to the job that updates the viewer content (clears the pending entry,
	 * etc.) after all deferred content has been retrieved. If the listener is
	 * already attached to a running job, it is not removed, but it will not be
	 * added to any subsequent jobs that are run.
	 * 
	 * This method has no effect if the listener was not previously added to the
	 * listener list.
	 * 
	 * @param listener
	 *            the listener to be removed
	 * @since 3.6
	 */
	public void removeUpdateCompleteListener(IJobChangeListener listener) {
		if (updateCompleteListenerList != null) {
			updateCompleteListenerList.remove(listener);
		}
	}

}
