/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.progress;

/**
 * The UIJob is a Job that runs within the UI Thread via an
 * asyncExec.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressMessages;

public abstract class UIJob extends Job {
	private Display display;


	/**
	 * Create a new instance of the receiver with the supplied name.
	 * @param name
	 */
	public UIJob(String name) {
		super(name);
	}

	/**
	 * Create a new instance of the receiver with the supplied
	 * Display.
	 * @param jobDisplay
	 * @param name
	 */
	public UIJob(Display jobDisplay,String name) {
		this(name);
		display = jobDisplay;
	}

	/**
	 * Convenience method to return a status for an exception.
	 * @param exception
	 * @return
	 */
	public static IStatus errorStatus(Throwable exception) {
		return new Status(
			IStatus.ERROR,
			PlatformUI.PLUGIN_ID,
			IStatus.ERROR,
			exception.getMessage(),
			exception);

	}
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
	 * Note: this message is marked final. Implementors should use
	 * runInUIThread() instead.
	 */
	public final IStatus run(final IProgressMonitor monitor) {

		Display asyncDisplay = getDisplay();

		if (asyncDisplay == null) {
			return Status.CANCEL_STATUS;
		}
		asyncDisplay.asyncExec(new Runnable() {
			public void run() {
				IStatus result = null;
				try {
					result = runInUIThread(monitor);
				} finally {
					if (result == null)
							result = new Status(
									IStatus.ERROR, 
									PlatformUI.PLUGIN_ID, 
									IStatus.ERROR, 
									ProgressMessages.getString("Error"), //$NON-NLS-1$
									null);
					done(result);
				}
			}
		});
		return Job.ASYNC_FINISH;
	}

	/**
	 * Run the job in the UI Thread.
	 * @param monitor
	 * @return IStatus
	 */
	public abstract IStatus runInUIThread(IProgressMonitor monitor);

	/**
	 * Set the display to execute the asyncExec in.
	 * @param runDisplay
	 */
	public void setDisplay(Display runDisplay) {
		display = runDisplay;
	}
	/**
	 * Get the display for use by the receiver.
	 * @return Display
	 */
	public Display getDisplay() {
		if (display != null)
			return display;
		IWorkbenchWindow windows[] =
			WorkbenchPlugin.getDefault().getWorkbench().getWorkbenchWindows();
		if (windows.length == 0)
			return null;
		else
			return windows[0].getShell().getDisplay();

	}

}
