/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;

/**
 * The TaskInfo is the info on a task with a job. It is assumed that there is
 * only one task running at a time - any previous tasks in a Job will be
 * deleted.
 */
public class TaskInfo extends SubTaskInfo {
	double preWork = 0;

	int totalWork = 0;

	/**
	 * Create a new instance of the receiver with the supplied total work and
	 * task name.
	 *
	 * @param parentJobInfo
	 * @param infoName
	 * @param total
	 */
	TaskInfo(JobInfo parentJobInfo, String infoName, int total) {
		super(parentJobInfo, infoName);
		totalWork = total;
	}

	/**
	 * Add the work increment to the total.
	 *
	 * @param workIncrement
	 */
	void addWork(double workIncrement) {

		// Don't bother if we are indeterminate
		if (totalWork == IProgressMonitor.UNKNOWN) {
			return;
		}
		preWork += workIncrement;

	}

	/**
	 * Add the amount of work to the recevier. Update a parent monitor by the
	 * increment scaled to the amount of ticks this represents.
	 *
	 * @param workIncrement
	 *            int the amount of work in the receiver
	 * @param parentMonitor
	 *            The IProgressMonitor that is also listening
	 * @param parentTicks
	 *            the number of ticks this monitor represents
	 */
	void addWork(double workIncrement, IProgressMonitor parentMonitor,
			int parentTicks) {
		// Don't bother if we are indeterminate
		if (totalWork == IProgressMonitor.UNKNOWN) {
			return;
		}

		addWork(workIncrement);
		parentMonitor.internalWorked(workIncrement * parentTicks / totalWork);
	}

	@Override
	String getDisplayString(boolean showProgress) {

		if (totalWork == IProgressMonitor.UNKNOWN) {
			return unknownProgress();
		}

		if (taskName == null) {
			return getDisplayStringWithoutTask(showProgress);
		}

		if (showProgress) {
			String[] messageValues = new String[3];
			messageValues[0] = String.valueOf(getPercentDone());
			messageValues[1] = jobInfo.getJob().getName();
			messageValues[2] = taskName;

			return NLS
					.bind(ProgressMessages.JobInfo_DoneMessage, messageValues);
		}
		String[] messageValues = new String[2];
		messageValues[0] = jobInfo.getJob().getName();
		messageValues[1] = taskName;

		return NLS.bind(ProgressMessages.JobInfo_DoneNoProgressMessage,
				messageValues);

	}

	/**
	 * Get the display String without the task name.
	 *
	 * @param showProgress
	 *            Whether or not we are showing progress
	 *
	 * @return String
	 */
	String getDisplayStringWithoutTask(boolean showProgress) {

		if (!showProgress || totalWork == IProgressMonitor.UNKNOWN) {
			return jobInfo.getJob().getName();
		}

		return NLS.bind(ProgressMessages.JobInfo_NoTaskNameDoneMessage, jobInfo
				.getJob().getName(), String.valueOf(getPercentDone()));
	}

	/**
	 * Return an integer representing the amount of work completed. If progress
	 * is indeterminate return IProgressMonitor.UNKNOWN.
	 *
	 * @return int IProgressMonitor.UNKNOWN or a value between 0 and 100.
	 */
	int getPercentDone() {
		if (totalWork == IProgressMonitor.UNKNOWN) {
			return IProgressMonitor.UNKNOWN;
		}

		return Math.min((int) (preWork * 100 / totalWork), 100);
	}

	/**
	 * Return the progress for a monitor whose totalWork is
	 * <code>IProgressMonitor.UNKNOWN</code>.
	 *
	 * @return String
	 */
	private String unknownProgress() {
		if (taskName == null) {
			return jobInfo.getJob().getName();
		}
		String[] messageValues = new String[2];
		messageValues[0] = jobInfo.getJob().getName();
		messageValues[1] = taskName;
		return NLS
				.bind(ProgressMessages.JobInfo_UnknownProgress, messageValues);

	}
}
