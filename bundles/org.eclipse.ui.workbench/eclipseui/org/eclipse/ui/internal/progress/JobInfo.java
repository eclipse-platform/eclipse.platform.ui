/*******************************************************************************
 * Copyright (c) 2003, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak <brockj@tpg.com.au> - Fix for Bug 123169 [Progress] NPE from JobInfo
 *     Martin W. Kirst <martin.kirst@s1998.tu-chemnitz.de> - jUnit test for Bug 361121 [Progress] DetailedProgressViewer's comparator violates its general contract
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

	private volatile boolean canceled;
	private final Queue<JobTreeElement> children = new ConcurrentLinkedQueue<>();

	private final Job job;

	private GroupInfo parent;

	private volatile Optional<TaskInfo> taskInfo;

	private ProgressManager progressManager;

	private FinishedJobs finishedJobs;

	// Default to no progress
	private int ticks = -1;

	/**
	 * Creates a top level JobInfo.
	 *
	 * @param enclosingJob the job to represent by this info
	 */
	protected JobInfo(Job enclosingJob) {
		this.job = enclosingJob;
		this.progressManager = ProgressManager.getInstance();
		this.finishedJobs = FinishedJobs.getInstance();
		this.taskInfo = Optional.empty();
	}

	@Override
	public int hashCode() {
		return job.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JobInfo) {
			return job.equals(((JobInfo) obj).job);
		}
		return false;
	}

	/**
	 * Adds the subtask to the receiver.
	 *
	 * @param subTaskName name for the sub task
	 */
	void addSubTask(String subTaskName) {
		children.add(new SubTaskInfo(this, subTaskName));
	}

	/**
	 * Adds the amount of work to the job info.
	 */
	void addWork(double workIncrement) {
		Optional<TaskInfo> optionalInfo = getTaskInfo();
		if (!optionalInfo.isPresent()) {
			return;
		}
		TaskInfo taskInfo = optionalInfo.get();
		if (parent == null || ticks < 1) {
			taskInfo.addWork(workIncrement);
		} else {
			taskInfo.addWork(workIncrement, parent, ticks);
		}
	}

	/**
	 * Begins the task called taskName with the supplied work.
	 */
	void beginTask(String taskName, int work) {
		taskInfo = Optional.of(new TaskInfo(this, taskName, work));
	}

	@Override
	public void cancel() {
		this.canceled = true;
		this.job.cancel();
		// Call the refresh so that this is updated immediately
		progressManager.refreshJobInfo(this);
	}

	/**
	 * Clears the collection of subtasks an the task info.
	 */
	void clearChildren() {
		children.clear();
	}

	void clearTaskInfo() {
		taskInfo.ifPresent(finishedJobs::remove);
		taskInfo = Optional.empty();
	}

	// for debugging only
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "('" + job.getName() //$NON-NLS-1$
				+ "' isUser=" + job.isUser() //$NON-NLS-1$
				+ " isBlocked=" + isBlocked() //$NON-NLS-1$
				+ " priority=" + job.getPriority() //$NON-NLS-1$
				+ ")"; //$NON-NLS-1$
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
	 * @return the blockedStatus.
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
		Optional<TaskInfo> optionalInfo = getTaskInfo();
		if (optionalInfo.isPresent()) {
			return optionalInfo.get().getDisplayStringWithoutTask(true);
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
		// By default return the first progress image.
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
	 * Returns the display string based on the current status and the name of the
	 * job.
	 *
	 * @param showProgress a boolean to indicate if we should show progress or not.
	 *
	 * @return String
	 */
	private String getDisplayStringWithStatus(boolean showProgress) {
		if (isCanceled()) {
			return NLS.bind(ProgressMessages.JobInfo_Cancelled, (new Object[] { getJob().getName() }));
		}
		IStatus blockedStatusLocal = getBlockedStatus();
		if (blockedStatusLocal != null) {
			return NLS.bind(ProgressMessages.JobInfo_Blocked,
					(new Object[] { getJob().getName(), blockedStatusLocal.getMessage() }));
		}
		if (getJob().getState() == Job.RUNNING) {
			Optional<TaskInfo> optionalInfo = getTaskInfo();
			if (!optionalInfo.isPresent()) {
				return getJob().getName();
			}
			return optionalInfo.get().getDisplayString(showProgress);
		}
		if (getJob().getState() == Job.SLEEPING) {
			return NLS.bind(ProgressMessages.JobInfo_Sleeping, (new Object[] { getJob().getName() }));
		}

		return NLS.bind(ProgressMessages.JobInfo_Waiting, (new Object[] { getJob().getName() }));
	}

	/**
	 * Returns the GroupInfo for the receiver if it' is active.
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
	 * Returns the job that the receiver is collecting data on.
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
	 * Returns the amount of progress we have had as a percentage. If there is no
	 * progress or it is indeterminate return IProgressMonitor.UNKNOWN.
	 *
	 * @return int
	 */
	int getPercentDone() {
		Optional<TaskInfo> optionalInfo = getTaskInfo();
		if (optionalInfo.isPresent()) {
			TaskInfo info = optionalInfo.get();
			if (info.totalWork == IProgressMonitor.UNKNOWN) {
				return IProgressMonitor.UNKNOWN;
			}
			if (info.totalWork == 0) {
				return 0;
			}
			return (int) info.preWork * 100 / info.totalWork;
		}
		return IProgressMonitor.UNKNOWN;
	}

	/**
	 * @return the taskInfo, never null
	 */
	Optional<TaskInfo> getTaskInfo() {
		return taskInfo;
	}

	@Override
	boolean hasChildren() {
		return !children.isEmpty();
	}

	@Override
	boolean isActive() {
		return getJob().getState() != Job.NONE;
	}

	/**
	 * Returns whether or not the receiver is blocked.
	 *
	 * @return boolean <code>true</code> if this is a currently blocked job.
	 */
	public boolean isBlocked() {
		return getBlockedStatus() != null;
	}

	/**
	 * Returns whether or not the job was cancelled in the UI.
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
	 * Sets the description of the blocking status.
	 *
	 * @param blockedStatus The IStatus that describes the blockage or
	 *                      <code>null</code>
	 */
	public void setBlockedStatus(IStatus blockedStatus) {
		this.blockedStatus = blockedStatus;
	}

	/**
	 * Sets the GroupInfo to be the group.
	 */
	void setGroupInfo(GroupInfo group) {
		parent = group;
	}

	/**
	 * Sets the name of the taskInfo.
	 */
	void setTaskName(String name) {
		taskInfo.ifPresent(info -> info.setTaskName(name));
	}

	/**
	 * Sets the number of ticks this job represents. Default is indeterminate (-1).
	 *
	 * @param ticks The ticks to set.
	 */
	public void setTicks(int ticks) {
		this.ticks = ticks;
	}
}
