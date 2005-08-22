/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Provides a threaded content provider for efficiently handling large tables.
 * The owner of this object is responsible for adding and removing the items in the
 * table (by invoking the add, remove, set, and change methods, respectively). However,
 * the changes are buffered and applied to the actual table incrementally
 * using a background thread. This keeps the UI responsive when manipulating very
 * large tables. 
 * 
 * <p>
 * Other objects should set the contents of the table by manipulating
 * this object rather than manipulating the table itself. Threading issues
 * are encapsulated internally.
 * </p>
 *
 */
class TableContentProvider implements IStructuredContentProvider {

    // NLS strings
    private static final String TABLE_SYNCHRONIZATION = 
    	MarkerMessages.TableContentProvider_TableSynchronization;

    private static final String UPDATING_TABLE_WIDGET = 
    	MarkerMessages.TableContentProvider_Updating;

    /**
     * Currently running update job.
     */
    private String description = ""; //$NON-NLS-1$

    /**
     * Comparator to use for sorting the view
     */
    private TableSorter sortOrder = null;

    // Pending changes
    private DeferredQueue queues;

    /**
     * This job disables redraw on the table
     */
    private Job disableUpdatesJob = new WorkbenchJob(TABLE_SYNCHRONIZATION) {
        public IStatus runInUIThread(IProgressMonitor monitor) {
            if (controlExists()) {
                getViewer().getTable().setRedraw(false);
            }

            return Status.OK_STATUS;
        }
    };

    /**
     * This job re-enables redraw on the table
     */
    private Job enableUpdatesJob = new WorkbenchJob(TABLE_SYNCHRONIZATION) {
        public IStatus runInUIThread(IProgressMonitor monitor) {
            if (controlExists()) {
                getViewer().getTable().setRedraw(true);
            }

            return Status.OK_STATUS;
        }
    };

    /**
     * This job is responsible for performing a single incremental update to the
     * viewer. It will either add, remove, or change items in the viewer depending
     * on the contents of the pending* sets, above. It is scheduled repeatedly by 
     * the OverallUpdateJob, below.
     */
    private class WidgetRefreshJob extends WorkbenchJob {

        /**
         * Number of items modified in the last update
         */
        int lastWorked = 0;

        /**
         * Remembers whether the viewer existed the last time this job was run
         */
        boolean controlExists = true;

        WidgetRefreshJob(String title) {
            super(title);
        }

        /**
         * Will be executed each time the update thread wakes up. This makes
         * a single incremental update to the viewer (ie: adds or removes a few items)
         */
        public IStatus runInUIThread(IProgressMonitor monitor) {

            // If we can't get the lock, terminate without blocking the UI thread.
            if (lock.getDepth() > 0) {
                lastWorked = 0;
                return Status.OK_STATUS;
            }

            lock.acquire();
            try {
                if (!PlatformUI.isWorkbenchRunning()) {
                    controlExists = false;
                } else {
                    controlExists = controlExists();
                    if (controlExists) {
                        lastWorked = updateViewer();
                    }
                }
            } finally {
                lock.release();
            }

            return Status.OK_STATUS;
        }
    }

    /**
     * Job that does the real work for individual updates
     */
    WidgetRefreshJob uiJob;

    /**
     * This job incrementally updates the viewer until all pending changes have
     * been applied. It repeatedly schedules WidgetRefreshJobs until there are no more
     * changes to apply. This job doesn't actually do any real work -- it simply
     * schedules updates and updates the progress bar.
     */
    RestartableJob updateJob;

    private ILock lock;

    /**
     * Creates a new TableContentProvider that will control the contents of the given
     * viewer. 
     * 
     * @param viewer
     * @param description user-readable string that will be included in progress monitors
     * @param service IWorkbenchSiteProgressService or <code>null</null>
     * 	 the service that this content provider will inform of 
     * 	updates.
     */
    public TableContentProvider(TableViewer viewer, String description,
            IWorkbenchSiteProgressService service) {
        this.queues = new DeferredQueue(viewer);
        this.description = description;

        uiJob = new WidgetRefreshJob(UPDATING_TABLE_WIDGET);
        uiJob.setPriority(Job.LONG);
        uiJob.setSystem(true);

        updateJob = new RestartableJob(TABLE_SYNCHRONIZATION,
                new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        doUpdate(monitor);
                    }
                }, service);

        lock = Platform.getJobManager().newLock();
    }

    /**
     * Sets the view's sorter (or null if no sorting is to be used)
     * 
     * @param c comparator that controls the view's sort order (or null if no sorting)
     */
    public void setSorter(TableSorter c) {
        if (sortOrder != c) {
            sortOrder = c;
            scheduleUpdate();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        return queues.getVisibleItems();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        //No state to dispose here
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer inputViewer, Object oldInput,
            Object newInput) {
        scheduleUpdate();
    }

    /**
     * Sets the contents of the table. Note that the changes may not become visible
     * immediately, as the viewer will actually be updated in a background thread.
     * 
     * @param newVisibleItems
     */
    public void set(Collection newVisibleItems, IProgressMonitor mon) {
        lock.acquire();

        try {
            queues.set(newVisibleItems, mon);

            scheduleUpdate();
        } finally {
            lock.release();
        }
    }

    /**
     * If the contents of the viewer have somehow become out-of-sync with the "visibleItems"
     * set, this method will restore synchronization.
     */
    private void resync() {
        if (controlExists()) {
            int count = queues.getViewer().getTable().getItemCount();
            if (count != queues.countVisibleItems()) {
                queues.getViewer().refresh();
            }
        }
    }

    /**
     * Causes the given collection of items to be refreshed
     * 
     * @param changes collection of objects that have changed
     */
    void change(Collection changes) {

        lock.acquire();
        try {
            // Ensure that this is never done in the user interface thread.
            //Assert.isTrue(Display.getCurrent() == null);

            queues.change(changes);
            scheduleUpdate();
        } finally {
            lock.release();
        }
    }

    /**
     * Returns the TableViewer being populated by this content provider
     * 
     * @return the TableViewer being populated by this content provider
     */
    private TableViewer getViewer() {
        return queues.getViewer();
    }

    /**
     * Returns true iff the control exists and has not yet been disposed
     * 
     * @return
     */
    private boolean controlExists() {
        Control control = getViewer().getControl();

        if (control == null || control.isDisposed()) {
            return false;
        }

        return true;
    }

    /**
     * Returns true iff this content provider contains changes that are not yet
     * reflected in the viewer.
     * 
     * @return true iff the reciever has unprocessed changes
     */
    public boolean hasPendingChanges() {
        return queues.hasPendingChanges() || sortOrder != queues.getSorter();
    }

    /**
     * Returns an estimate of the total work remaining (used for progress monitors)
     * 
     * @return
     */
    private int totalWork() {
        return queues.workRemaining() + 1;
    }

    /**
     * Starts the update thread... this will continue to make incremental changes
     * to the viewer until the pending* sets become empty. Does nothing if the
     * update thread is already running or if there are no changes to process.
     */
    private void scheduleUpdate() {
        if (hasPendingChanges()) {
            updateJob.schedule();
        }
    }

    /**
     * Cancels any pending changes to the viewer. The contents of the viewer
     * will be left in whatever state they are in at the time. Any changes that
     * have not yet been applied will be lost. It is a good idea to call this
     * method before performing a long computation that will ultimately invalidate
     * the contents of the viewer.
     */
    public void cancelPendingChanges() {
        updateJob.cancel();

        lock.acquire();
        try {
            queues.cancelPending();
        } finally {
            lock.release();
        }
    }

    private void doUpdate(IProgressMonitor monitor) throws InterruptedException {

        //Do not update if the workbench is shutdown
        if (!PlatformUI.isWorkbenchRunning())
            return;

        // This counter represents how many work units remain unused in the progress monitor.
        int remainingWorkUnits = 100000;

        monitor.beginTask(description, remainingWorkUnits);

        disableUpdatesJob.schedule();
        disableUpdatesJob.join();
        try {

            // Loop until there are no more changes to apply, the control is destroyed, the monitor is cancelled,
            // or another job takes over.
            while (hasPendingChanges() && !monitor.isCanceled()) {

                // Ensure that we aren't running in the UI thread
                //Assert.isTrue(Display.getCurrent() == null);

                try {

                    int totalWork;
                    lock.acquire();
                    try {
                        totalWork = totalWork();
                        if (sortOrder != queues.getSorter()) {
                            queues.setComparator(sortOrder);
                        }

                        SubProgressMonitor sub = new SubProgressMonitor(
                                monitor, 0);
                        queues.refreshQueues(sub);
                    } finally {
                        lock.release();
                    }

                    try {
                        uiJob.schedule();
                        // Wait for the current update job to complete before scheduling another
                        uiJob.join();
                    } catch (IllegalStateException e) {
                        // Ignore this exception -- it means that the Job manager was shut down, which is expected
                        // at the end of the application. Note that we need to check for this by catching the exception
                        // rather than using something like if (jobManagerIsShutDown())... since the job manager might
                        // be shut down in another thread between the time we evaluate the if statement and when
                        // we schedule the job.
                    }

                    // Estimate how much of the remaining work we'll be doing in this update,
                    // and update the progress bar appropriately.
                    int consumedUnits = uiJob.lastWorked * remainingWorkUnits
                            / totalWork;
                    monitor.worked(consumedUnits);
                    remainingWorkUnits -= consumedUnits;

                } catch (InterruptedException e) {
                    monitor.setCanceled(true);
                }

                if (!uiJob.controlExists) {
                    break;
                }
            }
        } finally {
            //Only send updates if we can send them to the workbench
            if (PlatformUI.isWorkbenchRunning()) {
                enableUpdatesJob.schedule();
                enableUpdatesJob.join();
            }

            monitor.done();
        }
    }

    /**
     * Performs a single update to the viewer. Based on the contents of the pending* queues,
     * items will either be removed, added, or refreshed in the viewer (in that order). This
     * should only be called within a synchronized block, since the various queues shouldn't
     * be modified during an update. This method is invoked repeatedly by jobs to gradually
     * apply the pending changes.
     */
    private int updateViewer() {

        int result;

        // Note that this method is only called when we have the lock so acquiring it here
        // does nothing... but we re-acquire it anyway in case future refactoring causes
        // this to be called when we don't own the lock.
        lock.acquire();
        try {
            if (getViewer().getSorter() != null) {
                getViewer().setSorter(null);
            }

            resync();

            result = queues.nextUpdate();
        } finally {
            lock.release();
        }

        return result;
    }
}
