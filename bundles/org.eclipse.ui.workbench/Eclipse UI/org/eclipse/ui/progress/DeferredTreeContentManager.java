/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.progress.PendingUpdateAdapter;
import org.eclipse.ui.internal.progress.ProgressMessages;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * The DeferredContentManager is a class that helps an ITreeContentProvider get
 * its deferred input.
 * 
 * <b>NOTE</b> AbstractTreeViewer#isExpandable may need to 
 * be implemented in AbstractTreeViewer subclasses with 
 * deferred content that use filtering as a call to 
 * #getChildren may be required to determine the correct 
 * state of the expanding control.
 * 
 * AbstractTreeViewers which use this class may wish to 
 * sacrifice accuracy of the expandable state indicator for the 
 * performance benefits of defering content.
 * 
 * @see IDeferredWorkbenchAdapter
 * @since 3.0
 */
public class DeferredTreeContentManager {
    ITreeContentProvider contentProvider;

    AbstractTreeViewer treeViewer;

    IWorkbenchSiteProgressService progressService;
    
    /**
     * The DeferredContentFamily is a class used to keep track of a
     * manager-object pair so that only jobs scheduled by the receiver
     * are cancelled by the receiver.
     * @since 3.1
     *
     */
    class DeferredContentFamily {
    	protected DeferredTreeContentManager manager;
		protected Object element;
		
		/**
		 * Create a new instance of the receiver to define a family
		 * for object in a particular scheduling manager.
		 * @param schedulingManager
		 * @param object
		 */
		DeferredContentFamily(DeferredTreeContentManager schedulingManager, Object object) {
			this.manager = schedulingManager;
			this.element = object;
    	}
    }

    /**
     * Create a new instance of the receiver using the supplied content
     * provider and viewer. Run any jobs using the site.
     * 
     * @param provider
     * @param viewer
     * @param site
     */
    public DeferredTreeContentManager(ITreeContentProvider provider,
            AbstractTreeViewer viewer, IWorkbenchPartSite site) {
        this(provider, viewer);
        Object siteService = site
                .getAdapter(IWorkbenchSiteProgressService.class);
        if (siteService != null)
            progressService = (IWorkbenchSiteProgressService) siteService;
    }

    /**
     * Create a new instance of the receiver using the supplied content
     * provider and viewer.
     * 
     * @param provider The content provider that will be updated
     * @param viewer The tree viewer that the results are added to
     */
    public DeferredTreeContentManager(ITreeContentProvider provider,
            AbstractTreeViewer viewer) {
        contentProvider = provider;
        treeViewer = viewer;
    }

    /**
     * Provides an optimized lookup for determining if an element has children.
     * This is required because elements that are populated lazilly can't
     * answer <code>getChildren</code> just to determine the potential for
     * children. Throw an AssertionFailedException if element is null.
     * 
     * @param element The Object being tested. This should not be
     * <code>null</code>.
     * @return boolean <code>true</code> if there are potentially children.
     * @throws RuntimeException if the element is null.
     */
    public boolean mayHaveChildren(Object element) {
    	Assert.isNotNull(element, ProgressMessages.DeferredTreeContentManager_NotDeferred); 
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
        if (element == null)
            return null;
        PendingUpdateAdapter placeholder = new PendingUpdateAdapter();
        startFetchingDeferredChildren(parent, element, placeholder);
        return new Object[] { placeholder };
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
        if (element instanceof IDeferredWorkbenchAdapter)
            return (IDeferredWorkbenchAdapter) element;
        if (!(element instanceof IAdaptable))
            return null;
        Object adapter = ((IAdaptable) element)
                .getAdapter(IDeferredWorkbenchAdapter.class);
        if (adapter == null)
            return null;
        return (IDeferredWorkbenchAdapter) adapter;
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
            /* (non-Javadoc)
             * @see org.eclipse.core.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
             */
            public IStatus run(IProgressMonitor monitor) {
                adapter.fetchDeferredChildren(parent, collector, monitor);
                if(monitor.isCanceled())
                	return Status.CANCEL_STATUS;
				return Status.OK_STATUS;
            }

           
            /* (non-Javadoc)
             * @see org.eclipse.core.jobs.Job#belongsTo(java.lang.Object)
             */
            public boolean belongsTo(Object family) {
            	if (family instanceof DeferredContentFamily) {
            		DeferredContentFamily contentFamily = (DeferredContentFamily) family;
            		if (contentFamily.manager == DeferredTreeContentManager.this)
            			return isParent(contentFamily, parent);
            	}
                return false;
               
            }

            /**
             * Check if the parent of element is equal to the parent used in
             * this job.
             * 
             * @param family
             *            The DeferredContentFamily that defines a potential 
             *            ancestor of the current parent in a particualr manager.
             * @param child
             *            The object to check against.
             * @return boolean <code>true</code> if the child or one of its
             * 			  parents are the same as the element of the family.
             */
            private boolean isParent(DeferredContentFamily family, Object child) {
                if (family.element.equals(child))
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
             * @param element
             *            The object we are adapting to.
             */
            private IWorkbenchAdapter getWorkbenchAdapter(Object element) {
                if (element instanceof IWorkbenchAdapter)
                    return (IWorkbenchAdapter) element;
                if (!(element instanceof IAdaptable))
                    return null;
                Object workbenchAdapter = ((IAdaptable) element)
                        .getAdapter(IWorkbenchAdapter.class);
                if (workbenchAdapter == null)
                    return null;
                return (IWorkbenchAdapter) workbenchAdapter;
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
        if (progressService == null)
            job.schedule();
        else
            progressService.schedule(job);
    }
    
    /**
     * Returns a name to use for the job that fetches children of the given parent.
     * Subclasses may override. Default job name is parent's label.
     * 
     * @param parent parent that children are to be fetched for
     * @param adapter parent's deferred adapter
     * @return job name
     */
    protected String getFetchJobName(Object parent, IDeferredWorkbenchAdapter adapter) {
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
                //Cancel the job if the tree viewer got closed
                if (treeViewer.getControl().isDisposed())
                    return Status.CANCEL_STATUS;
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
     * Run a job to clear the placeholder. This is used when the update
     * for the tree is complete so that the user is aware that no more 
     * updates are pending.
     * 
     * @param placeholder
     */
    protected void runClearPlaceholderJob(final PendingUpdateAdapter placeholder) {
        if (placeholder.isRemoved() || !PlatformUI.isWorkbenchRunning())
            return;
        //Clear the placeholder if it is still there
        WorkbenchJob clearJob = new WorkbenchJob(ProgressMessages.DeferredTreeContentManager_ClearJob) {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (!placeholder.isRemoved()) {
                    Control control = treeViewer.getControl();
                    if (control.isDisposed())
                        return Status.CANCEL_STATUS;
                    treeViewer.remove(placeholder);
                    placeholder.setRemoved(true);
                }
                return Status.OK_STATUS;
            }
        };
        clearJob.setSystem(true);
        clearJob.schedule();
    }

    /**
     * Cancel all jobs that are fetching content for the given parent or any of
     * its children.
     * 
     * @param parent
     */
    public void cancel(Object parent) {
        Platform.getJobManager().cancel(new DeferredContentFamily(this, parent));
    }

    /**
     * Create the element collector for the receiver.
     *@param parent
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
             *  (non-Javadoc)
             * @see org.eclipse.jface.progress.IElementCollector#add(java.lang.Object, org.eclipse.core.runtime.IProgressMonitor)
             */
            public void add(Object element, IProgressMonitor monitor) {
                add(new Object[] { element }, monitor);
            }

            /*
             *  (non-Javadoc)
             * @see org.eclipse.jface.progress.IElementCollector#add(java.lang.Object[], org.eclipse.core.runtime.IProgressMonitor)
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
}
