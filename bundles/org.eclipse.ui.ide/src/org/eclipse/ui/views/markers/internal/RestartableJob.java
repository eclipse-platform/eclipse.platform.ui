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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * Represents a job that can be restarted. When a job is "restarted", the currently running
 * instance is cancelled and a new instance is scheduled once the previous one terminates.
 * This does not inherit from the Jobs API. Instead of subclassing this class, a pointer to
 * a IRunnableWithProgress should be passed into the constructor. 
 */
public final class RestartableJob {
    IRunnableWithProgress runnable;

    Job theJob;

    boolean restartNeeded = false;

    private Object lock = new Object();

    private IProgressMonitor currentMonitor = null;

    IWorkbenchSiteProgressService progressService;

    /**
     * Constructs a new RestartableJob with the given name that will run the given
     * runnable.
     * 
     * @param name
     * @param newRunnable
     * @param service IWorkbenchSiteProgressService the service we are
     * going to use to show progress or <code>null</code>.
     */
    public RestartableJob(String name, IRunnableWithProgress newRunnable,
            IWorkbenchSiteProgressService service) {
        this.runnable = newRunnable;
        progressService = service;

        createJob(name);

        theJob.addJobChangeListener(new JobChangeAdapter() {
            public void done(IJobChangeEvent e) {
                synchronized (lock) {
                    currentMonitor = null;
                    if (restartNeeded) {
                        scheduleInService();
                    }
                }
            }
        });
    }

    /**
     * Instantiates the actual Job object.
     * 
     * @param name
     */
    private void createJob(String name) {
        theJob = new Job(name) {
            /* (non-Javadoc)
             * @see org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime.IProgressMonitor)
             */
            protected IStatus run(IProgressMonitor innerMonitor) {
                try {
                    synchronized (lock) {
                        restartNeeded = false;
                        currentMonitor = innerMonitor;
                    }
                    runnable.run(innerMonitor);
                } catch (InvocationTargetException e) {
                    return StatusUtil.newStatus(IStatus.ERROR, e.toString(), e
                            .getTargetException());
                } catch (InterruptedException e) {
                    return Status.CANCEL_STATUS;
                }
                if (innerMonitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                } else {
                    return Status.OK_STATUS;
                }
            }
            
            /* (non-Javadoc)
             * @see org.eclipse.core.internal.jobs.InternalJob#shouldSchedule()
             */
            public boolean shouldSchedule() {
            	return PlatformUI.isWorkbenchRunning();
            }
            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
             */
            public boolean shouldRun() {
            	return PlatformUI.isWorkbenchRunning();
            }
        };

        theJob.setPriority(Job.DECORATE);
        theJob.setSystem(true);
    }

    /**
     * Aborts the currently running job (if any) by cancelling its progress
     * monitor, and reschedules it. If there is no currently running job,
     * it will be started.
     */
    public void restart() {
        synchronized (lock) {
            if (currentMonitor == null) {
                scheduleInService();
            } else if (!restartNeeded) {
                restartNeeded = true;
                theJob.cancel();
            }
        }
    }

    /**
     * Schedules the job. Does nothing if the job is already running. 
     */
    public void schedule() {
        synchronized (lock) {
            if (currentMonitor == null) {
                scheduleInService();
            } else {
                if (currentMonitor.isCanceled()) {
                    restartNeeded = true;
                }
            }
        }
    }

    /**
     * Schedule theJob using the progress service if there
     * is one.
     */
    private void scheduleInService() {
        if (progressService == null)
            theJob.schedule();
        else
            progressService.schedule(theJob, 0, true);
    }

    /**
     * Cancels the job. If the job is currently running, it will be
     * terminated as soon as possible.
     */
    public void cancel() {
        synchronized (lock) {
            theJob.cancel();
            restartNeeded = false;
        }
    }
    
}
