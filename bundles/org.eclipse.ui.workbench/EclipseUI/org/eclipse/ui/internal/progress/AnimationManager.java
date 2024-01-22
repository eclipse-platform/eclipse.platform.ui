/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * The AnimationManager is the class that keeps track of the animation items to
 * update.
 */
public class AnimationManager {
	private static AnimationManager singleton;

	boolean animated = false;

	private IJobProgressManagerListener listener;

	IAnimationProcessor animationProcessor;

	WorkbenchJob animationUpdateJob;

	/**
	 * Returns the singleton {@link AnimationManager} instance
	 *
	 * @return the singleton {@link AnimationManager} instance
	 */
	public static AnimationManager getInstance() {
		if (singleton == null) {
			singleton = new AnimationManager();
		}
		return singleton;
	}

	/**
	 * Get the background color to be used.
	 *
	 * @param control The source of the display.
	 * @return Color
	 */
	static Color getItemBackgroundColor(Control control) {
		return control.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
	}

	AnimationManager() {

		animationProcessor = new ProgressAnimationProcessor(this);

		animationUpdateJob = new WorkbenchJob(ProgressMessages.AnimationManager_AnimationStart) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {

				if (animated) {
					animationProcessor.animationStarted();
				} else {
					animationProcessor.animationFinished();
				}
				return Status.OK_STATUS;
			}
		};
		animationUpdateJob.setSystem(true);

		listener = getProgressListener();
		ProgressManager.getInstance().addListener(listener);

	}

	/**
	 * Add an item to the list
	 *
	 * @param item animation item to add
	 */
	void addItem(final AnimationItem item) {
		animationProcessor.addItem(item);
	}

	/**
	 * Remove an item from the list
	 *
	 * @param item animation item to remove
	 */
	void removeItem(final AnimationItem item) {
		animationProcessor.removeItem(item);
	}

	/**
	 * Return whether or not the current state is animated.
	 *
	 * @return whether or not the current state is animated
	 */
	boolean isAnimated() {
		return animated;
	}

	/**
	 * Set whether or not the receiver is animated.
	 *
	 * @param bool receivers new animated state
	 */
	void setAnimated(final boolean bool) {
		animated = bool;
		animationUpdateJob.schedule(100);
	}

	/**
	 * Dispose the images in the receiver.
	 */
	void dispose() {
		setAnimated(false);
		ProgressManager.getInstance().removeListener(listener);
	}

	private IJobProgressManagerListener getProgressListener() {
		return new IJobProgressManagerListener() {
			Set<Job> jobs = new HashSet<>();

			@Override
			public void addJob(JobInfo info) {
				incrementJobCount(info);
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
				setAnimated(false);
				for (JobInfo currentInfo : manager.getJobInfos(showsDebug())) {
					addJob(currentInfo);
				}
			}

			@Override
			public void removeJob(JobInfo info) {
				decrementJobCount(info.getJob());
			}

			@Override
			public boolean showsDebug() {
				return false;
			}

			private void incrementJobCount(JobInfo info) {
				// Don't count the animate job itself
				if (isNotTracked(info)) {
					return;
				}
				if (jobs.isEmpty()) {
					setAnimated(true);
				}
				jobs.add(info.getJob());
			}

			/*
			 * Decrement the job count for the job
			 */
			private void decrementJobCount(Job job) {
				jobs.remove(job);
				if (jobs.isEmpty()) {
					setAnimated(false);
				}
			}

			/**
			 * If this is one of our jobs or not running then don't bother.
			 */
			private boolean isNotTracked(JobInfo info) {
				// We always track errors
				Job job = info.getJob();
				return job.getState() != Job.RUNNING || animationProcessor.isProcessorJob(job);
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

	/**
	 * Get the preferred width for widgets displaying the animation.
	 *
	 * @return int. Return 0 if there is no image data.
	 */
	int getPreferredWidth() {
		return animationProcessor.getPreferredWidth();
	}

}
