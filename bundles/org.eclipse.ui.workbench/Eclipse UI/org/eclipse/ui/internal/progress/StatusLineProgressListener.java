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

import java.util.*;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.internal.progress.ProgressMessages;

/**
 * The StatusLineProgressListener is a class that prints the current
 * progress on the status line.
 */
class StatusLineProgressListener implements IJobProgressManagerListener {

	List jobInfos = new ArrayList();

	private class RefreshJob extends UIJob {

		String message;

		/**
		 * Return a new instance of the receiver.
		 * @param name
		 */
		public RefreshJob() {
			super(ProgressMessages.getString("StatusLineProgressListener.Refresh")); //$NON-NLS-1$
			setPriority(Job.DECORATE);
			setSystem(true);

		}

		public IStatus runInUIThread(IProgressMonitor monitor) {
			IStatusLineManager manager = getStatusLineManager();
			if (manager == null)
				return Status.CANCEL_STATUS;
			manager.setMessage(message);
			return Status.OK_STATUS;
		}

		void setMessage(String newMessage) {
			message = newMessage;
		}

		/**
		 * Return the status line manager if there is one. Return
		 * null if one cannot be found.
		 * @return
		 */
		private IStatusLineManager getStatusLineManager() {
			IWorkbenchWindow window =
				WorkbenchPlugin
					.getDefault()
					.getWorkbench()
					.getActiveWorkbenchWindow();
			if (window != null && window instanceof WorkbenchWindow)
				return ((WorkbenchWindow) window).getStatusLineManager();
			return null;
		}

	}

	RefreshJob refreshJob = new RefreshJob();

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#add(org.eclipse.ui.internal.progress.JobInfo)
	 */
	public void add(JobInfo info) {
		jobInfos.add(info);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#refresh(org.eclipse.ui.internal.progress.JobInfo)
	 */
	public void refresh(JobInfo info) {

		if (info.getJob().getState() != Job.RUNNING)
			return;

		refreshJob.setMessage(info.getDisplayString());
		refreshJob.schedule(100);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#refreshAll()
	 */
	public void refreshAll() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#remove(org.eclipse.ui.internal.progress.JobInfo)
	 */
	public void remove(JobInfo info) {

		jobInfos.remove(info);

		refreshJob.setMessage(getNextMessage());
		refreshJob.schedule(100);
	}

	/**
	 * Return the String to update on the status line. If there
	 * is another running job return it's info - otherwise just
	 * return the empty String.
	 * @return
	 */
	private String getNextMessage() {

		Iterator remainingJobs = jobInfos.iterator();
		while (remainingJobs.hasNext()) {
			JobInfo next = (JobInfo) remainingJobs.next();
			if (next.getJob().getState() == Job.RUNNING)
				return next.getDisplayString();
		}
		return new String();

	}

}
