/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.internal.WorkbenchWindow;
/**
 * The WorkbenchProgressListener is a class that listens to progress on the
 * workbench and reports accordingly.
 */
class WorkbenchMonitorProvider {
	/**
	 * Get the progress monitor for a job. If it is a UIJob get the main
	 * monitor from the status line. Otherwise return no monitor.
	 * 
	 * @return IProgressMonitor
	 */
	IProgressMonitor getMonitor(Job job) {
		if (job instanceof UIJob)
			return getUIProgressMonitor(job.getName());
		else
			return new NullProgressMonitor();
	}
	/**
	 * Return the status line manager if there is one. 
	 * 
	 * @return IStatusLineWithProgressManager
	 */
	private IStatusLineManager getStatusLineManager() {
		if (PlatformUI.isWorkbenchRunning()) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null && window instanceof WorkbenchWindow) {
				return ((WorkbenchWindow) window).getStatusLineManager();
			}
		}
		return null;
	}
	/**
	 * Get a progress monitor for use with UIThreads. This monitor will use the
	 * status line directly if possible.
	 * 
	 * @param jobName.
	 *           Used if the task name is null.
	 * @return IProgressMonitor
	 */
	private IProgressMonitor getUIProgressMonitor(final String jobName) {
		return new IProgressMonitor() {
			IProgressMonitor internalMonitor;
			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String,
			 * int)
			 */
			public void beginTask(String name, int totalWork) {
				if (name == null || name.length() == 0)
					getInternalMonitor().beginTask(jobName, totalWork);
				else
					getInternalMonitor().beginTask(name, totalWork);
			}
			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#done()
			 */
			public void done() {
				getInternalMonitor().done();
			}
			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
			 */
			public void internalWorked(double work) {
				getInternalMonitor().internalWorked(work);
			}
			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
			 */
			public boolean isCanceled() {
				return getInternalMonitor().isCanceled();
			}
			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
			 */
			public void setCanceled(boolean value) {
				getInternalMonitor().setCanceled(value);
			}
			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
			 */
			public void setTaskName(String name) {
				getInternalMonitor().setTaskName(name);
			}
			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
			 */
			public void subTask(String name) {
				getInternalMonitor().subTask(name);
			}
			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
			 */
			public void worked(int work) {
				getInternalMonitor().worked(work);
			}
			/**
			 * Get the monitor that is being wrapped. This is called lazily as
			 * we will not be able to get the monitor for the workbench outside
			 * of the UI Thread and so we will have to wait until the monitor
			 * is accessed.
			 * 
			 * Return a NullProgressMonitor if the one from the workbench
			 * cannot be found.
			 * 
			 * @return IProgressMonitor
			 */
			private IProgressMonitor getInternalMonitor() {
				if (internalMonitor == null) {
					IStatusLineManager manager = getStatusLineManager();
					if (manager == null)
						internalMonitor = new NullProgressMonitor();
					else
						internalMonitor = manager.getProgressMonitor();
				}
				return internalMonitor;
			}
		};
	}
}
