/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak <brockj@tpg.com.au> - Fix for Bug 123169 [Progress] NPE from JobInfo
 *     Martin W. Kirst <martin.kirst@s1998.tu-chemnitz.de> - jUnit test for Bug 361121 [Progress] DetailedProgressViewer's comparator violates its general contract
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

/**
 * JobInfo is the class that keeps track of the tree structure for objects that
 * display job status in a tree.
 */
public class JobInfo extends JobTreeElement {

    private IStatus blockedStatus;

    private volatile boolean canceled = false;
    private List<SubTaskInfo> children = Collections.synchronizedList(new ArrayList<SubTaskInfo>());

    private Job job;

    private GroupInfo parent;

    private TaskInfo taskInfo;

    private ProgressManager progressManager;

    private FinishedJobs finishedJobs;

    //Default to no progress
    private int ticks = -1;

    /**
     * Create a top level JobInfo.
     *
     * @param enclosingJob
     */
	protected JobInfo(Job enclosingJob, ProgressManager progressManager,
	        FinishedJobs finishedJobs) {
        this.job = enclosingJob;
        this.progressManager = progressManager;
        this.finishedJobs = finishedJobs;
    }

    /**
     * Add the subtask to the receiver.
     *
     * @param subTaskName
     */
    void addSubTask(String subTaskName) {
        children.add(new SubTaskInfo(this, subTaskName));
    }

    /**
     * Add the amount of work to the job info.
     *
     * @param workIncrement
     */
    void addWork(double workIncrement) {
        if (taskInfo == null) {
			return;
		}
        if (parent == null || ticks < 1) {
			taskInfo.addWork(workIncrement);
		} else {
			taskInfo.addWork(workIncrement, parent, ticks);
		}
    }

    /**
     * Begin the task called taskName with the supplied work.
     *
     * @param taskName
     * @param work
     */
    void beginTask(String taskName, int work) {
        taskInfo = new TaskInfo(this, taskName, work);
    }

    @Override
	public void cancel() {
        this.canceled = true;
        this.job.cancel();
        //Call the refresh so that this is updated immediately
        progressManager.refreshJobInfo(this);
    }

    /**
     * Clear the collection of subtasks an the task info.
     */
    void clearChildren() {
        children.clear();
    }

    void clearTaskInfo() {
		finishedJobs.remove(taskInfo);
        taskInfo = null;
    }

    /**
     * Compare the the job of the receiver to job2.
     *
     * @param jobInfo
     *            The info we are comparing to
     * @return @see Comparable#compareTo(java.lang.Object)
     */
    private int compareJobs(JobInfo jobInfo) {

        Job job2 = jobInfo.getJob();

        //User jobs have top priority
        if (job.isUser()) {
            if (!job2.isUser()) {
				return -1;
			}
        } else {
            if (job2.isUser()) {
				return 1;
			}
        }

        //Show the blocked ones last
        if (isBlocked()) {
            if (!jobInfo.isBlocked()) {
				return 1;
			}
        } else {
            if (jobInfo.isBlocked()) {
				return -1;
			}
        }

        int thisPriority = job.getPriority();
		int otherPriority = job2.getPriority();
		// If equal prio, order by names
		if (thisPriority == otherPriority) {
            return job.getName().compareTo(job2.getName());
        }

        // order by priority
        if (thisPriority > otherPriority) {
			return -1;
		}
        return 1;
    }

    @Override
	public int compareTo(Object arg0) {

        if (!(arg0 instanceof JobInfo)) {
			return super.compareTo(arg0);
		}
        JobInfo element = (JobInfo) arg0;

        boolean thisCanceled = isCanceled();
		boolean anotherCanceled = element.isCanceled();
		if (thisCanceled && !anotherCanceled) {
			// If the receiver is cancelled then it is lowest priority
			return 1;
		} else if (!thisCanceled && anotherCanceled) {
			return -1;
		}

		int thisState = getJob().getState();
		int anotherState = element.getJob().getState();

		// if equal job state, compare other job attributes
		if (thisState == anotherState) {
			return compareJobs(element);
		}

		// ordering by job states, Job.RUNNING should be ordered first
		return (thisState > anotherState ? -1 : (thisState == anotherState ? 0 : 1));
    }

    /**
     * Dispose of the receiver.
     */
    void dispose() {
        if (parent != null) {
			parent.removeJobInfo(this);
		}
    }

    /**
     * Return the blocked status or <code>null</code> if there isn't one.
     *
     * @return Returns the blockedStatus.
     */
    public IStatus getBlockedStatus() {
        return blockedStatus;
    }

    @Override
	Object[] getChildren() {
        return children.toArray();
    }

    @Override
	String getCondensedDisplayString() {
    	TaskInfo info = getTaskInfo();
        if (info != null) {
			return info.getDisplayStringWithoutTask(true);
		}
        return getJob().getName();
    }

    @Override
	public Image getDisplayImage() {
        int done = getPercentDone();
        if (done > 0) {
            return super.getDisplayImage();
        }
        if (isBlocked()) {
			return JFaceResources.getImage(ProgressManager.BLOCKED_JOB_KEY);
		}
        int state = getJob().getState();
        if (state == Job.SLEEPING) {
			return JFaceResources.getImage(ProgressManager.SLEEPING_JOB_KEY);
		}
        if (state == Job.WAITING) {
			return JFaceResources.getImage(ProgressManager.WAITING_JOB_KEY);
		}
        //By default return the first progress image
        return super.getDisplayImage();

    }
    @Override
	String getDisplayString() {
    	return getDisplayString(true);
    }

    @Override
	String getDisplayString(boolean showProgress) {
        String name = getDisplayStringWithStatus(showProgress);
        if (job.isSystem()) {
			return NLS.bind(ProgressMessages.JobInfo_System, (new Object[] { name }));
		}
        return name;
    }

    /**
     * Get the display string based on the current status and the name of the
     * job.
     * @param showProgress a boolean to indicate if we should
     * show progress or not.
     *
     * @return String
     */
    private String getDisplayStringWithStatus(boolean showProgress) {
        if (isCanceled()) {
			return NLS.bind(ProgressMessages.JobInfo_Cancelled, (new Object[] { getJob().getName() }));
		}
        if (isBlocked()) {
			return NLS.bind(ProgressMessages.JobInfo_Blocked, (new Object[] { getJob().getName(),
			blockedStatus.getMessage() }));
		}
        if (getJob().getState() == Job.RUNNING) {
        	TaskInfo info = getTaskInfo();
            if (info == null) {
				return getJob().getName();
			}
            return info.getDisplayString(showProgress);
        }
        if (getJob().getState() == Job.SLEEPING) {
			return NLS.bind(ProgressMessages.JobInfo_Sleeping, (new Object[] { getJob().getName() }));
		}

        return NLS.bind(ProgressMessages.JobInfo_Waiting, (new Object[] { getJob().getName() }));

    }

    /**
     * Return the GroupInfo for the receiver if it' is active.
     *
     * @return GroupInfo or <code>null</code>.
     */
    GroupInfo getGroupInfo() {
        if (parent != null) {
			return parent;
		}
        return null;
    }

    /**
     * Return the job that the receiver is collecting data on.
     *
     * @return Job
     */
	public Job getJob() {
        return job;
    }

	@Override
	public GroupInfo getParent() {
        return parent;
    }

    /**
     * Return the amount of progress we have had as a percentage. If there is no
     * progress or it is indeterminate return IProgressMonitor.UNKNOWN.
     *
     * @return int
     */
    int getPercentDone() {
    	TaskInfo info = getTaskInfo();
        if (info != null){
        	if(info.totalWork == IProgressMonitor.UNKNOWN) {
				return IProgressMonitor.UNKNOWN;
			}
        	if(info.totalWork == 0) {
				return 0;
			}
            return (int) info.preWork * 100 / info.totalWork;
        }
        return IProgressMonitor.UNKNOWN;
    }

    /**
     * @return Returns the taskInfo.
     */
    TaskInfo getTaskInfo() {
        return taskInfo;
    }

    @Override
	boolean hasChildren() {
        return children.size() > 0;
    }

    /**
     * Return whether or not there is a task.
     *
     * @return boolean
     */
    boolean hasTaskInfo() {
        return taskInfo != null;
    }

    @Override
	boolean isActive() {
        return getJob().getState() != Job.NONE;
    }

    /**
     * Return whether or not the receiver is blocked.
     *
     * @return boolean <code>true</code> if this is a currently
     * blocked job.
     */
    public boolean isBlocked() {
        return getBlockedStatus() != null;
    }

    /**
     * Return whether or not the job was cancelled in the UI.
     *
     * @return boolean
     */
    public boolean isCanceled() {
        return canceled;
    }

    @Override
	public boolean isCancellable() {
        return super.isCancellable();
    }

    @Override
	boolean isJobInfo() {
        return true;
    }

    /**
     * Set the description of the blocking status.
     *
     * @param blockedStatus
     *            The IStatus that describes the blockage or <code>null</code>
     */
    public void setBlockedStatus(IStatus blockedStatus) {
        this.blockedStatus = blockedStatus;
    }

    /**
     * Set the GroupInfo to be the group.
     *
     * @param group
     */
    void setGroupInfo(GroupInfo group) {
        parent = group;
    }

    /**
     * Set the name of the taskInfo.
     *
     * @param name
     */
    void setTaskName(String name) {
        taskInfo.setTaskName(name);
    }

    /**
     * Set the number of ticks this job represents. Default is indeterminate
     * (-1).
     *
     * @param ticks
     *            The ticks to set.
     */
    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

}
