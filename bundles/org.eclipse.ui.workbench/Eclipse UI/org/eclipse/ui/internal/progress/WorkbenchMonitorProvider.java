/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IStatusLineWithProgressManager;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.progress.UIJob;

/**
 * The WorkbenchProgressListener is a class that listens to progress on the
 * workbench and reports accordingly.
 */
class WorkbenchMonitorProvider {

	private class RefreshJob extends UIJob {

		String message;
		boolean clear = false;

		/**
		 * Return a new instance of the receiver.
		 * 
		 * 
		 * @param name
		 */
		public RefreshJob() {
			super(ProgressMessages.getString("StatusLineProgressListener.Refresh")); //$NON-NLS-1$
			setPriority(Job.DECORATE);
			setSystem(true);

		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			IStatusLineWithProgressManager manager = getStatusLineManager();
			if (manager == null)
				return Status.CANCEL_STATUS;
			if (clear) {
				manager.clearProgress();
				clear = false;
			} else
				manager.setProgressMessage(message);
			return Status.OK_STATUS;
		}

		/**
		 * Set the message for the receiver. If it is a new message return a
		 * boolean.
	
		 * @param newMessage
		 * @return boolean. true if an update is required
		 */
		synchronized boolean setMessage(String newMessage) {
			if (newMessage.equals(message))
				return false;
			message = newMessage;
			return true;
		}

		synchronized void clearStatusLine() {
			clear = true;
		}

	}

	RefreshJob refreshJob = new RefreshJob();

	/**
	 * Get the progress monitor for a job. If it is a UIJob get the main
	 * monitor from the status line. Otherwise get a background monitor.
	 * 
	 * 
	 * @return IProgressMonitor
	 */
	IProgressMonitor getMonitor(Job job) {
		if (job instanceof UIJob) {
			IStatusLineManager manager = getStatusLineManager();
			if (manager != null)
				return manager.getProgressMonitor();
		}

		return getBackgroundProgressMonitor();
	}

	/**
	 * Return the status line manager if there is one. Return null if one
	 * cannot be found or it is not a IStatusLineWithProgressManager.
	 * 
	 * 
	 * 
	 * 
	 * @return IStatusLineWithProgressManager
	 */
	private IStatusLineWithProgressManager getStatusLineManager() {
		IWorkbenchWindow window = WorkbenchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window != null && window instanceof WorkbenchWindow) {
			IStatusLineManager manager = ((WorkbenchWindow) window).getStatusLineManager();
			if (manager instanceof IStatusLineWithProgressManager)
				return (IStatusLineWithProgressManager) manager;
		}
		return null;
	}

	/**
	 * Get a IProgressMonitor for the background jobs.
	 * 
	 * @return
	 */
	private IProgressMonitor getBackgroundProgressMonitor() {
		return new IProgressMonitor() {

			double allWork;
			double worked;
			String taskName;
			String subTask = ""; //$NON-NLS-1$

			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String,
			 * int)
			 */
			public void beginTask(String name, int totalWork) {
				allWork = totalWork;
				taskName = name;
				subTask = ""; //$NON-NLS-1$
				worked = 0;
				updateMessage();
			}

			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#done()
			 */
			public void done() {
				refreshJob.clearStatusLine();

			}

			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
			 */
			public void internalWorked(double work) {
				worked += work;
				updateMessage();
			}

			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
			 */
			public boolean isCanceled() {
				//TODO: Not clear if there will be cancel at this level
				return false;
			}

			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
			 */
			public void setCanceled(boolean value) {
				//TODO: Not clear if there will be cancel at this level

			}

			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
			 */
			public void setTaskName(String name) {
				taskName = name;
				updateMessage();

			}

			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
			 */
			public void subTask(String name) {
				//Don't show this granularity
			}

			/*
			 * (non-Javadoc) @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
			 */
			public void worked(int work) {
				worked += work;
				updateMessage();

			}

			/**
			 * Update the message for the receiver.
			 */
			private void updateMessage() {
				if(refreshJob.setMessage(getDisplayString()))
					refreshJob.schedule(100);
			}

			/**
			 * Get the display string for the task.
			 * 
			 * @return String
			 */
			String getDisplayString() {

				if (worked == IProgressMonitor.UNKNOWN) {
					if (taskName == null)
						return subTask;
					else {
						if (subTask.length() == 0)
							return taskName;
						else
							return ProgressMessages.format("MonitorProvider.twoValueUnknownMessage", new String[] { taskName, subTask }); //$NON-NLS-1$
					}
				} else {
					int done = (int) (worked * 100 / allWork);
					String percentDone = String.valueOf(done);

					String text = taskName;
					if (text == null)
						return ProgressMessages.format("MonitorProvider.oneValueMessage", new String[] { subTask, percentDone }); //$NON-NLS-1$
					else {
						if (subTask.length() == 0)
							return ProgressMessages.format("MonitorProvider.oneValueMessage", new String[] { taskName, percentDone }); //$NON-NLS-1$
					}
					return ProgressMessages.format("MonitorProvider.twoValueMessage", new String[] { taskName, subTask, String.valueOf(done)}); //$NON-NLS-1$

				}

			}
		};
	}
}
