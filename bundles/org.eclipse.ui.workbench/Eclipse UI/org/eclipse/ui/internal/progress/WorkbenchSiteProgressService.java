/**********************************************************************
 * Copyright (c) 2003,2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.progress;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.internal.PartSite;
/**
 * The WorkbenchSiteProgressService is the concrete implementation of the
 * WorkbenchSiteProgressService used by the workbench components.
 */
public class WorkbenchSiteProgressService
		implements
			IWorkbenchSiteProgressService,
			IJobBusyListener {
	PartSite site;
	private Collection busyJobs = Collections.synchronizedSet(new HashSet());
	private Object busyLock = new Object();
	IJobChangeListener listener;
	IPropertyChangeListener[] changeListeners = new IPropertyChangeListener[0];
	private Cursor waitCursor;
	private SiteUpdateJob updateJob;
	private class SiteUpdateJob extends WorkbenchJob {
		private boolean busy;
		private boolean useWaitCursor = false;
		Object lock = new Object();
		/**
		 * Set whether we are updating with the wait or busy cursor.
		 * 
		 * @param cursorState
		 */
		void setBusy(boolean cursorState) {
			synchronized (lock) {
				busy = cursorState;
			}
		}
		private SiteUpdateJob() {
			super(ProgressMessages
					.getString("WorkbenchSiteProgressService.CursorJob"));//$NON-NLS-1$
		}
		/**
		 * Get the wait cursor. Initialize it if required.
		 * 
		 * @return Cursor
		 */
		private Cursor getWaitCursor(Display display) {
			if (waitCursor == null) {
				waitCursor = new Cursor(display, SWT.CURSOR_APPSTARTING);
			}
			return waitCursor;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus runInUIThread(IProgressMonitor monitor) {
			Control control = site.getPane().getControl();
			if (control == null || control.isDisposed())
				return Status.CANCEL_STATUS;
			synchronized (lock) {
				//Update cursors if we are doing that
				if (useWaitCursor) {
					Cursor cursor = null;
					if (busy)
						cursor = getWaitCursor(control.getDisplay());
					control.setCursor(cursor);
				}
				site.getPane().setBusy(busy);
				IWorkbenchPart part = site.getPart();
				if (part instanceof WorkbenchPart)
					((WorkbenchPart) part).showBusy(busy);
			}
			return Status.OK_STATUS;
		}
		void clearCursors() {
			if (waitCursor != null) {
				waitCursor.dispose();
				waitCursor = null;
			}
		}
	}
	/**
	 * Create a new instance of the receiver with a site of partSite
	 * 
	 * @param partSite
	 *            PartSite.
	 */
	public WorkbenchSiteProgressService(final PartSite partSite) {
		site = partSite;
		updateJob = new SiteUpdateJob();
		updateJob.setSystem(true);
	}
	public void dispose() {
		if (updateJob != null)
			updateJob.cancel();
		if (waitCursor == null)
			return;
		waitCursor.dispose();
		waitCursor = null;
		ProgressManager.getInstance().removeListener(this);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#requestInUI(org.eclipse.ui.progress.UIJob,
	 *      java.lang.String)
	 */
	public IStatus requestInUI(UIJob job, String message) {
		return site.getWorkbenchWindow().getWorkbench().getProgressService()
				.requestInUI(job, message);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#busyCursorWhile(org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void busyCursorWhile(IRunnableWithProgress runnable)
			throws InvocationTargetException, InterruptedException {
		site.getWorkbenchWindow().getWorkbench().getProgressService()
				.busyCursorWhile(runnable);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IWorkbenchSiteProgressService#schedule(org.eclipse.core.runtime.jobs.Job,
	 *      long, boolean)
	 */
	public void schedule(Job job, long delay, boolean useHalfBusyCursor) {
		job.addJobChangeListener(getJobChangeListener(job, useHalfBusyCursor));
		job.schedule(delay);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IWorkbenchSiteProgressService#schedule(org.eclipse.core.runtime.jobs.Job,
	 *      int)
	 */
	public void schedule(Job job, long delay) {
		schedule(job, delay, false);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IWorkbenchSiteProgressService#schedule(org.eclipse.core.runtime.jobs.Job)
	 */
	public void schedule(Job job) {
		schedule(job, 0L, false);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IWorkbenchSiteProgressService#showBusyForFamily(java.lang.Object)
	 */
	public void showBusyForFamily(Object family) {
		ProgressManager.getInstance().addListenerToFamily(family, this);
	}
	/**
	 * Get the job change listener for this site.
	 * 
	 * @param job
	 * @param useHalfBusyCursor
	 * @return IJobChangeListener
	 */
	public IJobChangeListener getJobChangeListener(final Job job,
			boolean useHalfBusyCursor) {
		if (listener == null) {
			updateJob.useWaitCursor = useHalfBusyCursor;
			listener = new JobChangeAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
				 */
				public void aboutToRun(IJobChangeEvent event) {
					incrementBusy(event.getJob());
				}
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
				 */
				public void done(IJobChangeEvent event) {
					decrementBusy(event.getJob());
				}
			};
		}
		return listener;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IJobBusyListener#decrementBusy(org.eclipse.core.runtime.jobs.Job)
	 */
	public void decrementBusy(Job job) {
		synchronized (busyLock) {
			busyJobs.remove(job);
			if (busyJobs.size() > 0)
				return;
		}
		if (PlatformUI.isWorkbenchRunning()) {
			updateJob.setBusy(false);
			updateJob.schedule(100);
		} else
			updateJob.cancel();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.IJobBusyListener#incrementBusy(org.eclipse.core.runtime.jobs.Job)
	 */
	public void incrementBusy(Job job) {
		synchronized (busyLock) {
			busyJobs.add(job);
			//If it is greater than one we already set busy
			if (busyJobs.size() > 1)
				return;
		}
		if (PlatformUI.isWorkbenchRunning()) {
			updateJob.setBusy(true);
			updateJob.schedule(100);
		} else
			updateJob.cancel();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IWorkbenchSiteProgressService#warnOfContentChange()
	 */
	public void warnOfContentChange() {
		site.getPane().showHighlight();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#showInDialog(org.eclipse.swt.widgets.Shell,
	 *      org.eclipse.core.runtime.jobs.Job, boolean)
	 */
	public void showInDialog(Shell shell, Job job, boolean runImmediately) {
		site.getWorkbenchWindow().getWorkbench().getProgressService()
				.showInDialog(shell, job, runImmediately);
	}
}