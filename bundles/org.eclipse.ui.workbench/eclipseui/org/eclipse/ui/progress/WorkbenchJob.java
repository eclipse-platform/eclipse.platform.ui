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
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.progress;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * WorkbenchJob is a type of job that implements a done listener and does the
 * shutdown checks before scheduling. This is used if a job is not meant to run
 * when the Workbench is shutdown.
 *
 * @since 3.0
 */
public abstract class WorkbenchJob extends UIJob {

	/**
	 * Create a new instance of the receiver with the supplied display and name.
	 * Normally this constructor would not be used as it is best to let the job find
	 * the display from the workbench
	 *
	 * @param jobDisplay Display. The display to run the job with.
	 * @param name       String
	 */
	public WorkbenchJob(Display jobDisplay, String name) {
		super(jobDisplay, name);
		addDefaultJobChangeListener();
	}

	/**
	 * Add a new instance of the receiver with the supplied name.
	 *
	 * @param name String
	 */
	public WorkbenchJob(String name) {
		super(name);
		addDefaultJobChangeListener();
	}

	/**
	 * Add a job change listeners that handles a done event if the result was
	 * IStatus.OK.
	 */
	private void addDefaultJobChangeListener() {
		addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {

				// Abort if it is not running
				if (!PlatformUI.isWorkbenchRunning()) {
					return;
				}

				if (event.getResult().getCode() == IStatus.OK) {
					performDone(event);
				}
			}
		});
	}

	/**
	 * Perform done with the supplied event. This will only occur if the returned
	 * status was OK. This is called only if the job is finished with an IStatus.OK
	 * result and the workbench is still running.
	 *
	 * @param event IJobChangeEvent
	 */
	public void performDone(IJobChangeEvent event) {
		// Do nothing by default.
	}

	@Override
	public boolean shouldSchedule() {
		return super.shouldSchedule() && PlatformUI.isWorkbenchRunning();
	}

	@Override
	public boolean shouldRun() {
		return super.shouldRun() && PlatformUI.isWorkbenchRunning();
	}

}
