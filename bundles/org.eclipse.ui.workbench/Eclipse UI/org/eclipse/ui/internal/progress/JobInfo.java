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
	private IStatus blockedStatus;
	private boolean canceled = false;
	private GroupInfo parent;
	
	//Default to no progress
	private int ticks = -1;

	/**
	 * Return the job that the receiver is collecting data
	 * on.
	 * @return Job
	 */
	Job getJob() {
		return job;
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
		
		if(isCanceled())
			return ProgressMessages.format("JobInfo.Cancelled", //$NON-NLS-1$
				new Object[] { getJob().getName()});
					
		if (isBlocked())
			return ProgressMessages.format("JobInfo.Blocked", //$NON-NLS-1$
				new Object[] { getJob().getName(), blockedStatus.getMessage()});
		

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
		taskInfo = new TaskInfo(this, taskName, work);
	}

	/**
	 * Add the subtask to the receiver.
	 * @param subTaskName
	 */
	void addSubTask(String subTaskName) {
		children.add(new SubTaskInfo(this, subTaskName));
	}

	/**
	 * Add the amount of work to the job info.
	 * @param workIncrement
	 */
	void addWork(double workIncrement) {
		if (taskInfo == null)
			return;
		
		if(parent == null || ticks < 1)
			taskInfo.addWork(workIncrement);
		else
			taskInfo.addWork(workIncrement,parent,ticks);
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
		return parent;
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
		
		if(!(arg0 instanceof JobInfo))
			return super.compareTo(arg0);
		
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
	
	/**
	 * Return the amount of progress we have had as a percentage. 
	 * If there is no progress return -1.
	 * @return int
	 */
	int getPercentDone(){
		if(hasTaskInfo())
			return (int) taskInfo.preWork * 100 / taskInfo.totalWork;
		else return -1;
		
	}
	/**
	 * Return the blocked status or <code>null</code> if there isn't
	 * one.
	 * @return Returns the blockedStatus.
	 */
	public IStatus getBlockedStatus() {
		return blockedStatus;
	}

	/**
	 * Set the description of the blocking status.
	 * @param blockedStatus The IStatus that describes the blockage or
	 * 	<code>null</code>
	 */
	public void setBlockedStatus(IStatus blockedStatus) {
		this.blockedStatus = blockedStatus;
	}
	
	/**
	 * Return whether or not the receiver is blocked.
	 * @return
	 */
	public boolean isBlocked(){
		return getBlockedStatus() != null;
	}

	/**
	 * Return whether or not the job was cancelled in the UI.
	 * @return boolean
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Set the canceled flag to true.
	 */
	public void cancel() {
		this.canceled = true;
		this.job.cancel();
	}

	/**
	 * @return Returns the taskInfo.
	 */
	TaskInfo getTaskInfo() {
		return taskInfo;
	}

	/**
	 * Set the GroupInfo to be the group.
	 * @param group
	 */
	void setGroupInfo(GroupInfo group){
		parent = group;
	}
	
	/**
	 * Dispose of the receiver.
	 */
	void dispose(){
		if(parent != null)
			parent.removeJobInfo(this);
	}
	
	/**
	 * Return the GroupInfo for the receiver if it'
	 * is active.
	 * @return GroupInfo or <code>null</code>.
	 */
	GroupInfo getGroupInfo(){
		if(parent != null && parent.isActive())
			return parent;
		else
			return null;
	}
	/**
	 * Set the number of ticks this job represents.
	 * Default is indeterminate (-1).
	 * @param ticks The ticks to set.
	 */
	public void setTicks(int ticks) {
		this.ticks = ticks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#isActive()
	 */
	boolean isActive() {
		return getJob().getState() == Job.NONE;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.progress.JobTreeElement#getCondensedDisplayString()
	 */
	String getCondensedDisplayString() {
		if(hasTaskInfo())
			return getTaskInfo().getDisplayStringWithoutTask();
		else
			return getJob().getName();
	}

}
