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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;

import org.eclipse.ui.internal.PartSite;
/**
 * The WorkbenchSiteProgressService is the concrete 
 * implementation of the WorkbenchSiteProgressService
 * used by the workbench components.
 */
public class WorkbenchSiteProgressService implements IWorkbenchSiteProgressService {
	PartSite site;
	/**
	 * Create a new instance of the receiver with a site of partSite
	 * 
	 * @param partSite
	 *            PartSite.
	 */
	public WorkbenchSiteProgressService(PartSite partSite) {
		site = partSite;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#requestInUI(org.eclipse.ui.progress.UIJob,
	 *      java.lang.String)
	 */
	public IStatus requestInUI(UIJob job, String message) {
		return site.getWorkbenchWindow().getWorkbench().getProgressService().requestInUI(job,
				message);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IProgressService#busyCursorWhile(org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void busyCursorWhile(IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		site.getWorkbenchWindow().getWorkbench().getProgressService().busyCursorWhile(runnable);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IWorkbenchSiteProgressService#schedule(org.eclipse.core.runtime.jobs.Job,
	 *      int)
	 */
	public void schedule(Job job, long delay) {
		site.schedule(job, delay);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.IWorkbenchSiteProgressService#schedule(org.eclipse.core.runtime.jobs.Job)
	 */
	public void schedule(Job job) {
		schedule(job, 0L);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.progress.IWorkbenchSiteProgressService#useHalfBusyCursor(org.eclipse.core.runtime.jobs.Job)
	 */
	public void useHalfBusyCursor(Job job) {
		job.addJobChangeListener(new JobChangeAdapter() {
			private Cursor waitCursor;
			
			/**
			 * Get the wait cursor. Initialize it if required.
			 * @return
			 */
			private Cursor getWaitCursor(Display display) {
				if (waitCursor == null) {
					waitCursor = new Cursor(display, SWT.CURSOR_APPSTARTING);
				}
				return waitCursor;
			}
			
			/**
			 * Show the cursor. Use the wait cursor if useWaitCursor is
			 * true.
			 * @param useWaitCursor
			 */
			private void showCursor(final boolean useWaitCursor) {
				
				WorkbenchJob cursorJob = new WorkbenchJob(ProgressMessages.getString("WorkbenchSiteProgressService.CursorJob")){ //$NON-NLS-1$
					/* (non-Javadoc)
					 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
					 */
					public IStatus runInUIThread(IProgressMonitor monitor) {
						
						Control control =  site.getPane().getControl();
						if(control == null || control.isDisposed())
							return Status.CANCEL_STATUS;
						
						Cursor cursor = null;
						if(useWaitCursor){
							cursor = getWaitCursor(control.getDisplay());
						}
						
						control.setCursor(cursor);
						return Status.OK_STATUS;
					}
				};
				cursorJob.setSystem(true);
				cursorJob.schedule();
				
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void aboutToRun(IJobChangeEvent event) {
				showCursor(true);
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
			 */
			public void done(IJobChangeEvent event) {
				showCursor(false);
			}
			
		});
	}
}
