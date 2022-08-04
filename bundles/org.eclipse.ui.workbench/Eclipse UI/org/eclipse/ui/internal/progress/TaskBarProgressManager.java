/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Daniel Kruegler <daniel.kruegler@gmail.com> - Bug 471310
 ******************************************************************************/

package org.eclipse.ui.internal.progress;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TaskItem;
import org.eclipse.ui.progress.IProgressConstants;
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

	private List<Job> jobs = Collections.synchronizedList(new ArrayList<Job>());

	private Map<Job, JobInfo> jobInfoMap = Collections.synchronizedMap(new HashMap<Job, JobInfo>());

	private final TaskItem taskItem;

	private ImageDescriptor overlayDescriptor;

	private Image overlayImage;

	/**
	 * Create a progress manager for the task item.
	 *
	 * @param taskItem task item to manage
	 */
	public TaskBarProgressManager(TaskItem taskItem) {
		Assert.isNotNull(taskItem);
		this.taskItem = taskItem;
		animationUpdateJob = getAnimationUpdateJob();
		animationUpdateJob.setSystem(true);
		listener = getProgressListener();

		// Register the IJobProgressManagerListener so we can display progress
		// on the application TaskBar
		ProgressManager.getInstance().addListener(listener);

		taskItem.addDisposeListener(e -> dispose());
	}

	/**
	 * Remove the listener and stop the animation
	 */
	public void dispose() {
		ProgressManager.getInstance().removeListener(listener);
		setAnimated(false);
		disposeOverlay();
	}

	private WorkbenchJob getAnimationUpdateJob() {
		return new WorkbenchJob(ProgressMessages.AnimationManager_AnimationStart) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (isAnimated) {
					if (!taskItem.isDisposed() && !jobs.isEmpty()) {
						Job job = jobs.get(0);
						JobInfo jobInfo = jobInfoMap.get(job);
						if (job != null && jobInfo != null) {
							int percentDone = getPercentDone(jobInfo);
							Optional<TaskInfo> optionalInfo = jobInfo.getTaskInfo();
							if (percentDone == IProgressMonitor.UNKNOWN
									|| (optionalInfo.isPresent() && optionalInfo.get().totalWork == IProgressMonitor.UNKNOWN)) {
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
						updateImage(job);
					} else {
						updateImage(null);
					}
				} else {
					setProgressState(SWT.DEFAULT);
					updateImage(null);
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
						Optional<TaskInfo> optionalInfo = ((JobInfo) roots[0]).getTaskInfo();
						if (optionalInfo.isPresent()) {
							return optionalInfo.get().getPercentDone();
						}
					}
					return ((GroupInfo) info).getPercentDone();
				}
				return 0;
			}
		};
	}

	private void updateImage(Job job) {
		if (taskItem == null || taskItem.isDisposed())
			return;

		if (job == null) {
			disposeOverlay();
			taskItem.setOverlayImage(null);
			return;
		}

		// first check whether the job specifies image property
		// if not check with progress manager for its family
		ImageDescriptor descriptor = (ImageDescriptor) job.getProperty(IProgressConstants.ICON_PROPERTY);
		if (descriptor != null) {

			// if the description is same, do nothing.
			// Else dispose old one and store this
			if (!descriptor.equals(overlayDescriptor)) {
				disposeOverlay();
				setOverlay(descriptor);
			}
		} else if (ProgressManager.getInstance().getIconFor(job) != null) {
			disposeOverlay();
			Image newImage = ProgressManager.getInstance().getIconFor(job);
			taskItem.setOverlayImage(newImage);
		} else {
			disposeOverlay();
			taskItem.setOverlayImage(null);
		}
	}

	private void setOverlay(ImageDescriptor descriptor) {
		overlayDescriptor = descriptor;
		overlayImage = descriptor.createImage();
		taskItem.setOverlayImage(overlayImage);
	}

	private void disposeOverlay() {
		overlayDescriptor = null;
		if (overlayImage != null) {
			overlayImage.dispose();
			overlayImage = null;
		}
	}

	private IJobProgressManagerListener getProgressListener() {
		return new IJobProgressManagerListener() {

			@Override
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

			@Override
			public void refreshJobInfo(JobInfo info) {
				int state = info.getJob().getState();
				if (state == Job.RUNNING) {
					addJob(info);
				} else {
					removeJob(info);
				}
			}

			@Override
			public void refreshAll() {
				ProgressManager manager = ProgressManager.getInstance();
				jobs.clear();
				jobInfoMap.clear();
				setAnimated(false);
				for (JobInfo currentInfo : manager.getJobInfos(showsDebug())) {
					addJob(currentInfo);
				}
			}

			@Override
			public void removeJob(JobInfo info) {
				jobs.remove(info.getJob());
				jobInfoMap.remove(info.getJob());
				if (jobs.isEmpty()) {
					setAnimated(false);
				}
			}

			@Override
			public boolean showsDebug() {
				return false;
			}

			/**
			 * If the job isn't running or doesn't specify the
			 * IProgressConstants#SHOW_IN_TASKBAR_ICON_PROPERTY property, don't bother
			 * tracking it.
			 */
			private boolean isNotTracked(JobInfo info) {
				Job job = info.getJob();
				return job.getState() != Job.RUNNING || !shouldShowSystemProgress(info);
			}

			private boolean shouldShowSystemProgress(JobInfo info) {
				Boolean showInTaskBarIcon = Boolean.FALSE;
				Object property = info.getJob().getProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY);

				if (property instanceof Boolean) {
					showInTaskBarIcon = (Boolean) property;
				}
				return showInTaskBarIcon.booleanValue();

			}

			@Override
			public void addGroup(GroupInfo info) {
				// Don't care about groups
			}

			@Override
			public void removeGroup(GroupInfo group) {
				// Don't care about groups
			}

			@Override
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
