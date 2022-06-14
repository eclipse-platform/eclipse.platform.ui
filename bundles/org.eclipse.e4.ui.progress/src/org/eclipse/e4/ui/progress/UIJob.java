/*******************************************************************************
 * Copyright (c) 2003, 2017 IBM Corporation and others.
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
package org.eclipse.e4.ui.progress;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.progress.internal.ProgressMessages;
import org.eclipse.e4.ui.progress.internal.Services;
import org.eclipse.e4.ui.progress.internal.legacy.PlatformUI;
import org.eclipse.e4.ui.progress.internal.legacy.StatusUtil;
import org.eclipse.e4.ui.workbench.swt.DisplayUISynchronize;
import org.eclipse.swt.widgets.Display;

/**
 * The UIJob is a Job that runs within the UI Thread via an asyncExec.
 *
 * @since 3.0
 */
public abstract class UIJob extends Job {
	private Display cachedDisplay;

	/**
	 * Create a new instance of the receiver with the supplied name. The display
	 * used will be the one from the workbench if this is available. UIJobs with
	 * this constructor will determine their display at runtime.
	 *
	 * @param name the job name
	 */
	public UIJob(String name) {
		super(name);
	}

	/**
	 * Create a new instance of the receiver with the supplied Display.
	 *
	 * @param jobDisplay
	 *            the display
	 * @param name
	 *            the job name
	 * @see Job
	 */
	public UIJob(Display jobDisplay, String name) {
		this(name);
		setDisplay(jobDisplay);
	}

	/**
	 * Convenience method to return a status for an exception.
	 *
	 * @param exception the thrown exception
	 * @return IStatus an error status built from the exception
	 * @see Job
	 */
	public static IStatus errorStatus(Throwable exception) {
		return getStatus(exception);
	}

	/**
	 * Note: this message is marked final. Implementors should use runInUIThread()
	 * instead.
	 *
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public final IStatus run(final IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		UISynchronize uiSynchronize = getUiSynchronize();
		if (uiSynchronize == null) {
			return Status.CANCEL_STATUS;
		}
		uiSynchronize.asyncExec(() -> {
			IStatus result = null;
			Throwable throwable = null;
			try {
				// As we are in the UI Thread we can
				// always know what to tell the job.
				setThread(Thread.currentThread());
				if (monitor.isCanceled()) {
					result = Status.CANCEL_STATUS;
				} else {
					// TODO E4 - missing e4 replacement
					// UIStats.start(UIStats.UI_JOB, getName());
					result = runInUIThread(monitor);
				}

			} catch (Throwable t) {
				throwable = t;
			} finally {
				// TODO E4 - missing e4 replacement
				// UIStats.end(UIStats.UI_JOB, UIJob.this, getName());
				if (result == null) {
					result = new Status(IStatus.ERROR, IProgressConstants.PLUGIN_ID, IStatus.ERROR,
							ProgressMessages.InternalError, throwable);
				}
				done(result);
			}
		});
		return Job.ASYNC_FINISH;
	}

	/**
	 * Run the job in the UI Thread.
	 *
	 * @param monitor the monitor to be used for reporting progress and responding
	 *                to cancellation. The monitor is never <code>null</code>
	 * @return resulting status of the run. The result must not be <code>null</code>
	 */
	public abstract IStatus runInUIThread(IProgressMonitor monitor);

	/**
	 * Sets the display to execute the asyncExec in. Generally this is not'
	 * used if there is a valid display available via PlatformUI.isWorkbenchRunning().
	 *
	 * @param runDisplay
	 *            Display
	 * @see UIJob#getDisplay()
	 * @see PlatformUI#isWorkbenchRunning()
	 */
	public void setDisplay(Display runDisplay) {
		Assert.isNotNull(runDisplay);
		cachedDisplay = runDisplay;
	}

	/**
	 * Returns the display for use by the receiver when running in an
	 * asyncExec. If it is not set then the display set in the workbench
	 * is used.
	 * If the display is null the job will not be run.
	 *
	 * @return Display or <code>null</code>.
	 */
	public Display getDisplay() {
		if (cachedDisplay == null) {
			Services instance = Services.getInstance();
			if (instance != null) {
				cachedDisplay = instance.getDisplay();
			}
		}
		if (cachedDisplay == null) {
			cachedDisplay = Display.getCurrent();
		}
		if (cachedDisplay == null) {
			cachedDisplay = Display.getDefault();
		}
		return cachedDisplay;
	}

	/**
	 * Convenience method to return a status for a throwable.
	 *
	 * @param t the thrown throwable
	 * @return IStatus an error status built from the exception
	 */
	public static IStatus getStatus(Throwable t) {
		String message = StatusUtil.getLocalizedMessage(t);

		return newError(message, t);
	}

	/**
	 * Convenience method to return a status for a throwable including a message.
	 *
	 * @param message error message
	 * @param t       the thrown throwable
	 * @return IStatus an error status built from the exception
	 */
	public static IStatus newError(String message, Throwable t) {
		String pluginId = IProgressConstants.PLUGIN_ID;
		int errorCode = IStatus.OK;

		// If this was a CoreException, keep the original plugin ID and error
		// code
		if (t instanceof CoreException) {
			CoreException ce = (CoreException) t;
			pluginId = ce.getStatus().getPlugin();
			errorCode = ce.getStatus().getCode();
		}

		return new Status(IStatus.ERROR, pluginId, errorCode, message,
				StatusUtil.getCause(t));
	}

	/**
	 * Returns UI synchronizer
	 *
	 * @return UI synchronizer
	 */
	protected UISynchronize getUiSynchronize() {
		Services instance = Services.getInstance();
		if (instance == null) {
			return new DisplayUISynchronize(getDisplay());
		}
		return instance.getUISynchronize();
	}

}
