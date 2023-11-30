/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
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
package org.eclipse.e4.ui.dialogs.filteredtree;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

/**
 * Merge of UIJob and WokbenchJob minus tracking whether the workbench is
 * running - do not use for long running jobs!
 *
 * @since 1.2
 */
public abstract class BasicUIJob extends Job {

	private Display cachedDisplay;

	/**
	 * Create a new instance of the receiver with the supplied name. The display
	 * used will be the one from the workbench if this is available. UIJobs with
	 * this constructor will determine their display at runtime.
	 *
	 * @param name
	 *            the job name
	 */
	public BasicUIJob(String name, Display display) {
		super(name);
		this.cachedDisplay = display;
	}

	/**
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 *      Note: this message is marked final. Implementors should use
	 *      runInUIThread() instead.
	 */
	@Override
	public final IStatus run(final IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		Display asyncDisplay = (cachedDisplay == null) ? getDisplay()
				: cachedDisplay;
		if (asyncDisplay == null || asyncDisplay.isDisposed()) {
			return Status.CANCEL_STATUS;
		}
		asyncDisplay.asyncExec(() -> {
			IStatus result = null;
			try {
				// As we are in the UI Thread we can
				// always know what to tell the job.
				setThread(Thread.currentThread());
				if (monitor.isCanceled()) {
					result = Status.CANCEL_STATUS;
				} else {
					result = runInUIThread(monitor);
				}
			} finally {
				done(result);
			}
		});
		return Job.ASYNC_FINISH;
	}

	/**
	 * Run the job in the UI Thread.
	 *
	 * @return IStatus
	 */
	public abstract IStatus runInUIThread(IProgressMonitor monitor);

	/**
	 * Returns the display for use by the receiver when running in an asyncExec.
	 * If it is not set then the display set in the workbench is used. If the
	 * display is null the job will not be run.
	 *
	 * @return Display or <code>null</code>.
	 */
	public Display getDisplay() {
		return (cachedDisplay != null) ? cachedDisplay : Display.getCurrent();
	}
}
