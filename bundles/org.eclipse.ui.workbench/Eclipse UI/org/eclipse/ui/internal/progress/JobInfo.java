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
	private ArrayList children = new ArrayList();
	private Job job;
	private TaskInfo taskInfo;
	private IStatus status;

	static int RUNNING_STATUS = 0;
	static int PENDING_STATUS = 1;
	static int DONE_STATUS = 2;
	//	IStatus.ERROR = 4 so we keep our constants lower for sorting

	/**
	 * Create a new status for the supplied job with the
	 * code.
	 * @param code. One of RUNNING_STATUS, PENDING_STATUS,
	 *  DONE_STATUS or IStatus.ERROR.
		 * @param Job
	 */
	private static IStatus createStatus(int code, Job job) {
		return new Status(
			IStatus.INFO,
			PlatformUI.PLUGIN_ID,
			code,
			job.getName(),
			null);
	}

	/**
	 * Return the job that the receiver is collecting data
	 * on.
	 * @return Job
	 */
	Job getJob() {
		return job;
	}

	/**
	 * Return the current status of the receiver.
	 * @return IStatus
	 */
	IStatus getStatus() {
		return status;
	}

	/**
	 * Return whether or not there is a task.
	 * @return boolean
	 */
	boolean hasTaskInfo() {
		return taskInfo != null;
	}

	/**
	 * Set the name of the taskInfo.
	 * @param name
	 */
	void setTaskName(String name) {
		taskInfo.setTaskName(name);
	}

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
		if(job.isSystem())
			return ProgressMessages.format("JobInfo.System", //$NON-NLS-1$
					new Object[] { status.getMessage()});
		if (status.getCode() == PENDING_STATUS)
			return ProgressMessages.format("JobInfo.Pending", //$NON-NLS-1$
			new Object[] { status.getMessage()});
		if (status.getCode() == IStatus.ERROR)
			return ProgressMessages.format("JobInfo.Error", //$NON-NLS-1$
			new Object[] { job.getName(), status.getMessage()});

		if (taskInfo == null)
			return status.getMessage();
		else
			return taskInfo.getDisplayString();
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
		children.add(new SubTaskInfo(this.job, subTaskName));
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
	void clearChildren() {
		children.clear();
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

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		JobInfo element = (JobInfo) arg0;
		if (element.getStatus() == getStatus())
			return getJob().getName().compareTo(getJob().getName());
		else //Lower codes are shown higher (@see static fields)
			return getStatus().getCode() - element.getStatus().getCode();
	}
}
