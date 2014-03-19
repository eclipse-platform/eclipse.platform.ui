/*******************************************************************************
 * Copyright (c) 2003, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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

        animationUpdateJob = new UIJob(ProgressMessages.AnimationManager_AnimationStart) {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
             */
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
        progressManager.addListener(listener);


    }

    /**
     * Add an item to the list
     * 
     * @param item
     */
    void addItem(final AnimationItem item) {
        animationProcessor.addItem(item);
    }

    /**
     * Remove an item from the list
     * 
     * @param item
     */
    void removeItem(final AnimationItem item) {
        animationProcessor.removeItem(item);
    }

    /**
     * Return whether or not the current state is animated.
     * 
     * @return boolean
     */
    boolean isAnimated() {
        return animated;
    }

    /**
     * Set whether or not the receiver is animated.
     * 
     * @param boolean
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
        progressManager.removeListener(listener);
    }

    private IJobProgressManagerListener getProgressListener() {
        return new IJobProgressManagerListener() {
            Set<Job> jobs = Collections.synchronizedSet(new HashSet<Job>());

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#addJob(org.eclipse.ui.internal.progress.JobInfo)
             */
            public void addJob(JobInfo info) {
                incrementJobCount(info);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#refreshJobInfo(org.eclipse.ui.internal.progress.JobInfo)
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
             * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#refreshAll()
             */
            public void refreshAll() {
                jobs.clear();
                setAnimated(false);
                JobInfo[] currentInfos = progressManager.getJobInfos(showsDebug());
                for (int i = 0; i < currentInfos.length; i++) {
                    addJob(currentInfos[i]);
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#remove(org.eclipse.ui.internal.progress.JobInfo)
             */
            public void removeJob(JobInfo info) {
                decrementJobCount(info.getJob());
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#showsDebug()
             */
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

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#addGroup(org.eclipse.ui.internal.progress.GroupInfo)
             */
            public void addGroup(GroupInfo info) {
                //Don't care about groups
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#removeGroup(org.eclipse.ui.internal.progress.GroupInfo)
             */
            public void removeGroup(GroupInfo group) {
                //Don't care about groups
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.ui.internal.progress.IJobProgressManagerListener#refreshGroup(org.eclipse.ui.internal.progress.GroupInfo)
             */
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
