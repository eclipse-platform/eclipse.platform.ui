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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * The ProgressControl is a class that holds onto a canvas and
 * animates or disables as required.
 */
public class ProgressControl {

	private AnimatedCanvas canvas;
	private static final String PROGRESS_FOLDER = "icons/full/progress/"; //$NON-NLS-1$
	private static final String RUNNING_ICON = "running.gif"; //$NON-NLS-1$
	private static final String BACKGROUND_ICON = "back.gif"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver and register it with
	 * the JobManager.
	 */
	public ProgressControl() {
		super();
		Platform.getJobManager().addJobChangeListener(new JobChangeAdapter() {

			int jobCount = 0;
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#aboutToRun(org.eclipse.core.runtime.jobs.Job)
			 */
			public void aboutToRun(Job job) {
				incrementJobCount(job);
			}

			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#finished(org.eclipse.core.runtime.jobs.Job, int)
			 */
			public void done(Job job, IStatus result) {
				decrementJobCount(job);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.core.runtime.jobs.IJobListener#paused(org.eclipse.core.runtime.jobs.Job)
			 */
			public void paused(Job job) {
				decrementJobCount(job);

			}

			private void incrementJobCount(Job job) {
				//Don't count the animate job itself
				if (job instanceof AnimateJob)
					return;

				if (jobCount == 0)
					setEnabledImage();
				jobCount++;
			}

			private void decrementJobCount(Job job) {
				//Don't count the animate job itself
				if (job instanceof AnimateJob)
					return;
				if (jobCount == 1)
					setDisabledImage();
				jobCount--;
			}

		});
	}

	/**
	 * Set the image on the canvas to the disabled image.
	 */
	public void setDisabledImage() {
		canvas.setAnimated(false);
	}

	/**
	 * Set the image on the canvas to the enabled image.
	 */
	public void setEnabledImage() {
		canvas.setAnimated(true);

	}

	/**
	 * Dispose the canvas.
	 */
	public void dispose() {
		canvas.dispose();
	}

	/**
	 * Get the AnimatedCanvas that this control wraps.
	 * @return AnimatedCanvas
	 */
	public AnimatedCanvas getCanvas() {
		return canvas;
	}

	/**
	 * Create the canvas for the receiver in parent.
	 * @param parent
	 */
	public void createCanvas(Composite parent) {
		URL iconsRoot =
			Platform.getPlugin(PlatformUI.PLUGIN_ID).find(
				new Path(PROGRESS_FOLDER));
		try {
			URL runningRoot = new URL(iconsRoot, RUNNING_ICON);
			URL backRoot = new URL(iconsRoot, BACKGROUND_ICON);
			canvas = new AnimatedCanvas(runningRoot, backRoot);
			canvas.createCanvas(parent);
		} catch (MalformedURLException e) {
			Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog().log(
				new Status(
					IStatus.ERROR,
					PlatformUI.PLUGIN_ID,
					IStatus.ERROR,
					e.getMessage(),
					e));
			return;
		}
	}
}
