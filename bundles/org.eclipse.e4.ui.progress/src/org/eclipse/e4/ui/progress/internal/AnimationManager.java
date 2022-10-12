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
 *     Sebastian Sampaoli <seba.sampaoli@gmail.com> Bug - 428355
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.progress.UIJob;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;

/**
 * The AnimationManager is the class that keeps track of the animation items to
 * update.
 */
@Creatable
@Singleton
public class AnimationManager {

	boolean animated = false;

	private IJobProgressManagerListener listener;

	IAnimationProcessor animationProcessor;

	Job animationUpdateJob;

	@Inject
	ProgressManager progressManager;

	/**
	 * Get the background color to be used.
	 *
	 * @param control
	 *            The source of the display.
	 * @return Color
	 */
	static Color getItemBackgroundColor(Control control) {
		return control.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
	}

	@PostConstruct
	void init() {

		animationProcessor = new ProgressAnimationProcessor(this);

		animationUpdateJob = UIJob.create(ProgressMessages.AnimationManager_AnimationStart, monitor -> {
			if (animated) {
				animationProcessor.animationStarted();
			} else {
				animationProcessor.animationFinished();
			}
		});
		animationUpdateJob.setSystem(true);

		listener = getProgressListener();
		progressManager.addListener(listener);


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
	@PreDestroy
	void dispose() {
		setAnimated(false);
		progressManager.removeListener(listener);
	}

	private IJobProgressManagerListener getProgressListener() {
		return new IJobProgressManagerListener() {
			Set<Job> jobs = Collections.synchronizedSet(new HashSet<Job>());

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
				jobs.clear();
				setAnimated(false);
				JobInfo[] currentInfos = progressManager.getJobInfos(showsDebug());
				for (JobInfo currentInfo : currentInfos) {
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
				//Don't count the animate job itself
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
				//We always track errors
				Job job = info.getJob();
				return job.getState() != Job.RUNNING
						|| animationProcessor.isProcessorJob(job);
			}

			@Override
			public void addGroup(GroupInfo info) {
				//Don't care about groups
			}

			@Override
			public void removeGroup(GroupInfo group) {
				//Don't care about groups
			}

			@Override
			public void refreshGroup(GroupInfo info) {
				//Don't care about groups
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
