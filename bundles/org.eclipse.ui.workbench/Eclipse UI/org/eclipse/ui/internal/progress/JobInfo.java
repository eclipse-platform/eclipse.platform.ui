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

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

/**
 * JobInfo is the class that keeps track of the tree structure 
 * for objects that display job status in a tree.
 */
class JobInfo extends JobTreeElement {
	ArrayList children = new ArrayList();
	Job job;
	TaskInfo taskInfo;

	private static IStatus createStatus(int code, Job job) {
		return new Status(
			IStatus.INFO,
			PlatformUI.PLUGIN_ID,
			code,
			job.getName(),
			null);
	}

	static int PENDING_STATUS = 0;
	static int RUNNING_STATUS = 1;
	static int DONE_STATUS = 2;
	IStatus status;

	/**
	 * Create a top level JobInfo.
	 * @param taskName
	 */
	JobInfo(Job enclosingJob) {
		this.job = enclosingJob;
		status = createStatus(PENDING_STATUS, enclosingJob);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getDisplayString()
	 */
	String getDisplayString() {
		if (status.getCode() == PENDING_STATUS)
			return ProgressMessages.format(
				"JobInfo.Pending", //$NON-NLS-1$
				new Object[] { status.getMessage()});
		if (status.getCode() == IStatus.ERROR)
			return ProgressMessages.format(
				"JobInfo.Error", //$NON-NLS-1$
				new Object[] { job.getName(), status.getMessage()});

		return status.getMessage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getChildren()
	 */
	Object[] getChildren() {
		return children.toArray();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#hasChildren()
	 */
	boolean hasChildren() {
		return children.size() > 0;
	}

	/**
	 * Begin the task called taskName with the supplied work.
	 * @param taskName
	 * @param work
	 */
	void beginTask(String taskName, int work) {
		taskInfo = new TaskInfo(job, taskName, work);
	}

	/**
	 * Add the subtask to the receiver.
	 * @param subTaskName
	 */
	void addSubTask(String subTaskName) {
		children.add(subTaskName);
	}

	/**
	 * Add the amount of work to the job info.
	 * @param workIncrement
	 */
	void addWork(double workIncrement) {
		if (taskInfo != null)
			taskInfo.addWork(workIncrement);
	}
	/**
	 * Clear the collection of subtasks an the task info.
	 */
	void clear() {
		children.clear();
		this.taskInfo = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getParent()
	 */
	Object getParent() {
		return null;
	}

	/**
	 * Set the status to running.
	 */
	void setRunning() {
		status = createStatus(RUNNING_STATUS, job);
	}
	/**
	 * Set the status to error.
	 */
	void setError(IStatus errorStatus) {
		status = errorStatus;
	}
	/**
	 * Set the status to done.
	 */
	void setDone() {
		status = createStatus(DONE_STATUS, job);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#isJobInfo()
	 */
	boolean isJobInfo() {
		return true;
	}
}
