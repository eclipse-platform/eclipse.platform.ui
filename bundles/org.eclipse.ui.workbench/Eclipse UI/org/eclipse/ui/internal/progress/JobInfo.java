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
import org.eclipse.core.runtime.jobs.Job;

/**
 * JobInfo is the class that keeps track of the tree structure 
 * for objects that display job status in a tree.
 */
class JobInfo extends JobTreeElement {
	private ArrayList children = new ArrayList();
	private Job job;
	private TaskInfo taskInfo;
	private IStatus errorStatus;

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
	IStatus getErrorStatus() {
		return errorStatus;
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getDisplayString()
	 */
	String getDisplayString() {
		String name =  getDisplayStringWithStatus();
		if (job.isSystem())
		//Append with a system tag if system
			return ProgressMessages.format("JobInfo.System", //$NON-NLS-1$
				new Object[] { getJob().getName()});
		else
			return name;
	}
	
	/**
	 * Get the display string based on the current status and the name of the job.
	 * @return String
	 */

	private String getDisplayStringWithStatus() {
		
		if (errorStatus != null)
			return ProgressMessages.format("JobInfo.Error", //$NON-NLS-1$
			new Object[] { getJob().getName(), errorStatus.getMessage()});

		if (getJob().getState() == Job.RUNNING) {
			if (taskInfo == null)
				return getJob().getName();
			else
				return taskInfo.getDisplayString();
		} else {
			if (getJob().getState() == Job.SLEEPING)
				return ProgressMessages.format("JobInfo.Sleeping", //$NON-NLS-1$
					new Object[] { getJob().getName() });
			else
				return ProgressMessages.format("JobInfo.Waiting", //$NON-NLS-1$
					new Object[] { getJob().getName() });
		}
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
	 * Set the status to error.
	 */
	void setError(IStatus status) {
		errorStatus = status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#isJobInfo()
	 */
	boolean isJobInfo() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#isJobInfo()
	 */
	void clearTaskInfo() {
		taskInfo = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		JobInfo element = (JobInfo) arg0;
		if (element.getJob().getState() == getJob().getState())
			return getJob().getName().compareTo(getJob().getName());
		else { //If the receiver is running and the other isn't show it higer
			if (getJob().getState() == Job.RUNNING)
				return -1;
			else
				return 1;
		}
	}
}
