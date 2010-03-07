/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.TaskItem;
import org.eclipse.ui.progress.IProgressConstants2;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The TaskBarProgressManager is the class that displays progress in the
 * application TaskBar if the job specifies that it should show progress (@see
 * {@link IProgressConstants2#SHOW_IN_TASKBAR_ICON_PROPERTY}
 * 
 * @since 3.6
 */
public class TaskBarProgressManager {

	private IJobProgressManagerListener listener;

	private WorkbenchJob animationUpdateJob;

	private boolean isAnimated = false;

	private List jobs = Collections.synchronizedList(new ArrayList());

	private Map jobInfoMap = Collections.synchronizedMap(new HashMap());

	private final TaskItem taskItem;

	public TaskBarProgressManager(TaskItem taskItem) {
		Assert.isNotNull(taskItem);
		this.taskItem = taskItem;
		animationUpdateJob = getAnimationUpdateJob();
		animationUpdateJob.setSystem(true);
		listener = getProgressListener();

		// Register the IJobProgressManagerListener so we can display progress
		// on the application TaskBar
		ProgressManager.getInstance().addListener(listener);

		taskItem.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
	}

	/**
	 * Remove the listener and stop the animation
	 */
	public void dispose() {
		ProgressManager.getInstance().removeListener(listener);
		setAnimated(false);
	}

	private WorkbenchJob getAnimationUpdateJob() {
		return new WorkbenchJob(ProgressMessages.AnimationManager_AnimationStart) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.
			 * runtime .IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (isAnimated) {
					if (!taskItem.isDisposed() && !jobs.isEmpty()) {
						Job job = (Job) jobs.get(0);
						JobInfo jobInfo = (JobInfo) jobInfoMap.get(job);
						if (job != null && jobInfo != null) {
							int percentDone = getPercentDone(jobInfo);
							if (percentDone == IProgressMonitor.UNKNOWN
									|| (jobInfo.hasTaskInfo() && jobInfo.getTaskInfo().totalWork == IProgressMonitor.UNKNOWN)) {
								setProgressState(SWT.INDETERMINATE);
							} else {
								setProgressState(SWT.NORMAL);
								if (!taskItem.isDisposed()) {
									taskItem.setProgress(percentDone);
								}
							}
						} else {
							setProgressState(SWT.DEFAULT);
						}
					}
				} else {
					setProgressState(SWT.DEFAULT);
				}

				if (isAnimated && taskItem != null && !taskItem.isDisposed()) {
					schedule(400);
				}
				return Status.OK_STATUS;
			}

			private void setProgressState(int state) {
				if (!taskItem.isDisposed() && taskItem.getProgressState() != state) {
					taskItem.setProgressState(SWT.DEFAULT);
					taskItem.setProgressState(state);
				}
			}

			private int getPercentDone(JobTreeElement info) {
				if (info.isJobInfo()) {
					return ((JobInfo) info).getPercentDone();
				}

				if (info.hasChildren()) {
					Object[] roots = ((GroupInfo) info).getChildren();
					if (roots.length == 1 && roots[0] instanceof JobTreeElement) {
						TaskInfo ti = ((JobInfo) roots[0]).getTaskInfo();
						if (ti != null) {
							return ti.getPercentDone();
						}
					}
					return ((GroupInfo) info).getPercentDone();
				}
				return 0;
			}
		};
	}

	private IJobProgressManagerListener getProgressListener() {
		return new IJobProgressManagerListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.internal.progress.IJobProgressManagerListener#
			 * addJob(org.eclipse.ui.internal.progress.JobInfo)
			 */
			public void addJob(JobInfo info) {
				// Don't count the animate job itself
				if (isNotTracked(info)) {
					return;
				}
				if (jobs.isEmpty()) {
					setAnimated(true);
				}
				if (!jobs.contains(info.getJob())) {
					jobs.add(info.getJob());
				}
				jobInfoMap.put(info.getJob(), info);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.internal.progress.IJobProgressManagerListener#
			 * refreshJobInfo(org.eclipse.ui.internal.progress.JobInfo)
			 */
			public void refreshJobInfo(JobInfo info) {
				int state = info.getJob().getState();
				if (state == Job.RUNNING) {
					addJob(info);
				} else {
					removeJob(info);
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.internal.progress.IJobProgressManagerListener#
			 * refreshAll()
			 */
			public void refreshAll() {
				ProgressManager manager = ProgressManager.getInstance();
				jobs.clear();
				jobInfoMap.clear();
				setAnimated(false);
				JobInfo[] currentInfos = manager.getJobInfos(showsDebug());
				for (int i = 0; i < currentInfos.length; i++) {
					addJob(currentInfos[i]);
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.internal.progress.IJobProgressManagerListener#
			 * remove(org.eclipse.ui.internal.progress.JobInfo)
			 */
			public void removeJob(JobInfo info) {
				jobs.remove(info.getJob());
				jobInfoMap.remove(info.getJob());
				if (jobs.isEmpty()) {
					setAnimated(false);
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.internal.progress.IJobProgressManagerListener#
			 * showsDebug()
			 */
			public boolean showsDebug() {
				return false;
			}

			/**
			 * If the job isn't running or doesn't specify the
			 * IProgressConstants#SHOW_IN_TASKBAR_ICON_PROPERTY property, don't
			 * bother tracking it.
			 */
			private boolean isNotTracked(JobInfo info) {
				Job job = info.getJob();
				return job.getState() != Job.RUNNING || !shouldShowSystemProgress(info);
			}

			private boolean shouldShowSystemProgress(JobInfo info) {
				Boolean showInTaskBarIcon = Boolean.FALSE;
				Object property = info.getJob().getProperty(
						IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY);

				if (property instanceof Boolean) {
					showInTaskBarIcon = (Boolean) property;
				}
				return showInTaskBarIcon.booleanValue();

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.internal.progress.IJobProgressManagerListener#
			 * addGroup(org.eclipse.ui.internal.progress.GroupInfo)
			 */
			public void addGroup(GroupInfo info) {
				// Don't care about groups
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.internal.progress.IJobProgressManagerListener#
			 * removeGroup(org.eclipse.ui.internal.progress.GroupInfo)
			 */
			public void removeGroup(GroupInfo group) {
				// Don't care about groups
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.internal.progress.IJobProgressManagerListener#
			 * refreshGroup(org.eclipse.ui.internal.progress.GroupInfo)
			 */
			public void refreshGroup(GroupInfo info) {
				// Don't care about groups
			}
		};
	}

	private synchronized void setAnimated(boolean animated) {
		isAnimated = animated;
		animationUpdateJob.schedule();
	}
}
